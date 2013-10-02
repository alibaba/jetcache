/**
 * Created on  13-09-09 15:59
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class CacheHandler implements InvocationHandler {

    private Object src;
    private CacheProviderFactory cacheProviderFactory;

    private CacheConfig cacheConfig;
    private HashMap<String, CacheAnnoConfig> configMap;

    public CacheHandler(Object src, CacheConfig cacheConfig, CacheProviderFactory cacheProviderFactory) {
        this.src = src;
        this.cacheConfig = cacheConfig;
        this.cacheProviderFactory = cacheProviderFactory;
    }

    public CacheHandler(Object src, HashMap<String, CacheAnnoConfig> configMap, CacheProviderFactory cacheProviderFactory) {
        this.src = src;
        this.configMap = configMap;
        this.cacheProviderFactory = cacheProviderFactory;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        CacheInvokeContext context = null;
        boolean enableCacheFlag = false;
        if (cacheConfig != null) {
            context = new CacheInvokeContext();
            context.cacheConfig = cacheConfig;
            enableCacheFlag = false;
        } else {
            String sig = ClassUtil.getMethodSig(method);
            CacheAnnoConfig cac = configMap.get(sig);
            if (cac != null) {
                context = new CacheInvokeContext();
                context.cacheConfig = cac.getCacheConfig();
                enableCacheFlag = cac.isEnableCacheContext();
            }
        }
        if (context == null) {
            return method.invoke(src, args);
        } else {
            context.args = args;
            context.cacheProviderFactory = cacheProviderFactory;
            context.method = method;
            context.src = src;

            if (enableCacheFlag) {
                try {
                    CacheContextSupport.enable();
                    return invoke(context);
                } finally {
                    CacheContextSupport.disable();
                }
            } else {
                return invoke(context);
            }
        }
    }

    public static Object invoke(CacheInvokeContext context) throws Throwable {
        if (context.cacheConfig != null && (context.cacheConfig.isEnabled() || CacheContextSupport.isEnabled())) {
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

        CacheProvider cacheProvider = context.cacheProviderFactory.getCache(context.cacheConfig.getArea());
        String subArea = ClassUtil.getSubArea(context.cacheConfig, context.method);
        String key = cacheProvider.getKeyGenerator().getKey(context.args);
        boolean hit = getFromCache(context, cacheProvider, subArea, key);

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
        } else if (context.result == null && !context.cacheConfig.isCacheNullValue()) {
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

        updateCache(context, cacheProvider, subArea, key);
        return context.result;
    }

    private static void updateCache(CacheInvokeContext context, CacheProvider cacheProvider, String subArea, String key) {
        context.localResult = null;
        context.remoteResult = null;
        if (context.needUpdateLocal) {
            context.localResult = cacheProvider.getLocalCache().put(context.cacheConfig, subArea, key, context.result);
        }
        if (context.needUpdateRemote) {
            context.remoteResult = cacheProvider.getRemoteCache().put(context.cacheConfig, subArea, key, context.result);
        }
        if (context.cacheProviderFactory.getCacheMonitor() != null && (context.localResult != null || context.remoteResult != null)) {
            context.cacheProviderFactory.getCacheMonitor().onPut(context.cacheConfig, subArea, key, context.result, context.localResult, context.remoteResult);
        }
    }

    private static boolean getFromCache(CacheInvokeContext context, CacheProvider cacheProvider, String subArea, String key) {
        if (context.cacheConfig.getCacheType() == CacheType.REMOTE) {
            CacheResult result = cacheProvider.getRemoteCache().get(context.cacheConfig, subArea, key);
            context.remoteResult = result.getResultCode();
            if (result.isSuccess()) {
                context.result = result.getValue();
            }
        } else {
            CacheResult result = cacheProvider.getLocalCache().get(context.cacheConfig, subArea, key);
            context.localResult = result.getResultCode();
            if (result.isSuccess()) {
                context.result = result.getValue();
            } else {
                if (context.cacheConfig.getCacheType() == CacheType.BOTH) {
                    result = cacheProvider.getRemoteCache().get(context.cacheConfig, subArea, key);
                    context.remoteResult = result.getResultCode();
                    if (result.isSuccess()) {
                        context.result = result.getValue();
                    }
                }
            }
        }
        if (context.cacheProviderFactory.getCacheMonitor() != null) {
            context.cacheProviderFactory.getCacheMonitor().onGet(context.cacheConfig, subArea, key, context.localResult, context.remoteResult);
        }

        return context.localResult == CacheResultCode.SUCCESS || context.remoteResult == CacheResultCode.SUCCESS;
    }

    private static boolean needUpdate(CacheResultCode code) {
        return code != null && (code == CacheResultCode.NOT_EXISTS || code == CacheResultCode.EXPIRED);
    }

    private static Object invokeOrigin(CacheInvokeContext context) throws Throwable {
        if (context.invoker == null) {
            return context.method.invoke(context.src, context.args);
        } else {
            return context.invoker.invoke();
        }
    }


}
