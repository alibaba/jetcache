/**
 * Created on  13-09-09 15:59
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.CacheContext;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.support.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class CacheHandler implements InvocationHandler {

    private Object src;
    private GlobalCacheConfig globalCacheConfig;

    private CacheInvokeConfig cacheInvokeConfig;
    private HashMap<String, CacheInvokeConfig> configMap;

    private static class CacheContextSupport extends CacheContext {
        void _enable(){
            CacheContext.enable();
        }

        void _disable(){
            CacheContext.disable();
        }

        boolean _isEnabled(){
            return CacheContext.isEnabled();
        }
    }
    private static CacheContextSupport cacheContextSupport = new CacheContextSupport();

    public CacheHandler(Object src, CacheConfig cacheConfig, GlobalCacheConfig globalCacheConfig) {
        this.src = src;
        cacheInvokeConfig = new CacheInvokeConfig();
        cacheInvokeConfig.cacheConfig = cacheConfig;
        cacheInvokeConfig.init();
        this.globalCacheConfig = globalCacheConfig;
    }

    public CacheHandler(Object src, HashMap<String, CacheInvokeConfig> configMap, GlobalCacheConfig globalCacheConfig) {
        this.src = src;
        this.configMap = configMap;
        this.globalCacheConfig = globalCacheConfig;
    }

    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        CacheInvokeContext context = null;
        if (cacheInvokeConfig != null) {
            context = globalCacheConfig.createCacheInvokeContext();
            context.cacheInvokeConfig = cacheInvokeConfig;
        } else {
            String sig = ClassUtil.getMethodSig(method, globalCacheConfig.getHidePackages());
            CacheInvokeConfig cac = configMap.get(sig);
            if (cac != null) {
                context = globalCacheConfig.createCacheInvokeContext();
                context.cacheInvokeConfig = cac;
            }
        }
        if (context == null) {
            return method.invoke(src, args);
        } else {
            context.args = args;
            context.globalCacheConfig = globalCacheConfig;
            context.method = method;
            context.target = src;
            return invoke(context);
        }
    }

    public static Object invoke(CacheInvokeContext context) throws Throwable {
        if (context.cacheInvokeConfig.enableCacheContext) {
            try {
                cacheContextSupport._enable();
                return doInvoke(context);
            } finally {
                cacheContextSupport._disable();
            }
        } else {
            return doInvoke(context);
        }
    }

    private static Object doInvoke(CacheInvokeContext context) throws Throwable{
        CacheConfig cacheConfig = context.cacheInvokeConfig.cacheConfig;
        if (cacheConfig != null && (cacheConfig.isEnabled() || cacheContextSupport._isEnabled())) {
            return invokeWithCache(context);
        } else {
            return invokeOrigin(context);
        }
    }

    private static Object invokeWithCache(CacheInvokeContext context)
            throws Throwable {
        if (!ExpressionUtil.evalCondition(context)) {
            return invokeOrigin(context);
        }

        CacheConfig cacheConfig = context.cacheInvokeConfig.cacheConfig;

        CacheProvider cacheProvider = context.globalCacheConfig.getCache(cacheConfig.getArea());
        String subArea = ClassUtil.getSubArea(cacheConfig, context.method, context.globalCacheConfig.getHidePackages());
        String key = (String) cacheProvider.getKeyGenerator().generateKey(context.args);

        Cache localCache = cacheConfig.getCacheType() == CacheType.LOCAL ? cacheProvider.getLocalCache().getCache(subArea) : null;
        Cache remoteCache = cacheConfig.getCacheType() != CacheType.LOCAL ? cacheProvider.getRemoteCache().getCache(subArea) : null;


        boolean hit = getFromCache(context, subArea, localCache, remoteCache, key);

        context.needUpdateLocal = false;
        context.needUpdateRemote = false;

        if (!hit) {
            context.result = invokeOrigin(context);
            if (ExpressionUtil.evalUnless(context)) {
                context.needUpdateLocal = needUpdate(context.localResult);
                context.needUpdateRemote = needUpdate(context.remoteResult);
            } else {
                return context.result;
            }
        } else if (context.result == null && !cacheConfig.isCacheNullValue()) {
            context.result = invokeOrigin(context);
            if (ExpressionUtil.evalUnless(context)) {
                context.needUpdateLocal = context.localResult != null && context.localResult != CacheResultCode.FAIL;
                context.needUpdateRemote = context.remoteResult != null && context.remoteResult != CacheResultCode.FAIL;
            } else {
                return context.result;
            }
        } else {
            if (ExpressionUtil.evalUnless(context)) {
                context.needUpdateLocal = needUpdate(context.localResult);
                context.needUpdateRemote = false;
            } else {
                context.result = invokeOrigin(context);
                if (ExpressionUtil.evalUnless(context)) {
                    context.needUpdateLocal = context.localResult != null;
                    context.needUpdateRemote = context.remoteResult != null;
                } else {
                    return context.result;
                }
            }
        }

        updateCache(context, subArea, localCache ,remoteCache, key);
        return context.result;
    }

    private static void updateCache(CacheInvokeContext context,String subArea, Cache localCache, Cache remoteCache, String key) {
        context.localResult = null;
        context.remoteResult = null;
        CacheConfig cacheConfig = context.cacheInvokeConfig.cacheConfig;
        int expire = context.getCacheInvokeConfig().cacheConfig.getExpire();
        if (context.needUpdateLocal) {
            context.localResult = localCache.PUT(key, context.result, expire);
        }
        if (context.needUpdateRemote) {
            context.remoteResult = remoteCache.PUT(key, context.result, expire);
        }
        if (context.globalCacheConfig.getCacheMonitor() != null && (context.localResult != null || context.remoteResult != null)) {
            context.globalCacheConfig.getCacheMonitor().onPut(cacheConfig, subArea, key, context.result, context.localResult, context.remoteResult);
        }
    }

    private static boolean getFromCache(CacheInvokeContext context, String subArea, Cache localCache, Cache remoteCache, String key) {
        CacheConfig cacheConfig = context.cacheInvokeConfig.cacheConfig;
        if (cacheConfig.getCacheType() == CacheType.REMOTE) {
            CacheResult result = remoteCache.GET(key);
            context.remoteResult = result.getResultCode();
            if (result.isSuccess()) {
                context.result = result.getValue();
            }
        } else {
            CacheResult result = localCache.GET(key);
            context.localResult = result.getResultCode();
            if (result.isSuccess()) {
                context.result = result.getValue();
            } else {
                if (cacheConfig.getCacheType() == CacheType.BOTH) {
                    result = localCache.GET(key);
                    context.remoteResult = result.getResultCode();
                    if (result.isSuccess()) {
                        context.result = result.getValue();
                    }
                }
            }
        }
        if (context.globalCacheConfig.getCacheMonitor() != null) {
            context.globalCacheConfig.getCacheMonitor().onGet(cacheConfig, subArea, key, context.localResult, context.remoteResult);
        }

        return context.localResult == CacheResultCode.SUCCESS || context.remoteResult == CacheResultCode.SUCCESS;
    }

    private static boolean needUpdate(CacheResultCode code) {
        return code != null && (code == CacheResultCode.NOT_EXISTS || code == CacheResultCode.EXPIRED);
    }

    private static Object invokeOrigin(CacheInvokeContext context) throws Throwable {
        if (context.invoker == null) {
            return context.method.invoke(context.target, context.args);
        } else {
            return context.invoker.invoke();
        }
    }


}
