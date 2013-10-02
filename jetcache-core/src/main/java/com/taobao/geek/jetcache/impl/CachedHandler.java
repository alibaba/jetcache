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
class CachedHandler implements InvocationHandler {

    private Object src;
    private CacheProviderFactory cacheProviderFactory;

    private CacheConfig cacheConfig;
    private HashMap<String, CacheAnnoConfig> configMap;

    private static class GetCacheResult {
        boolean needUpdateLocal = false;
        boolean needUpdateRemote = false;
        CacheResultCode localResult = null;
        CacheResultCode remoteResult = null;
        Object value = null;
    }

    public CachedHandler(Object src, CacheConfig cacheConfig, CacheProviderFactory cacheProviderFactory) {
        this.src = src;
        this.cacheConfig = cacheConfig;
        this.cacheProviderFactory = cacheProviderFactory;
    }

    public CachedHandler(Object src, HashMap<String, CacheAnnoConfig> configMap, CacheProviderFactory cacheProviderFactory) {
        this.src = src;
        this.configMap = configMap;
        this.cacheProviderFactory = cacheProviderFactory;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        CacheInvokeContext context = null;
        boolean enableCacheInContext = false;
        if (cacheConfig != null) {
            context = new CacheInvokeContext();
            context.cacheConfig = cacheConfig;
            enableCacheInContext = false;
        } else {
            String sig = ClassUtil.getMethodSig(method);
            CacheAnnoConfig cac = configMap.get(sig);
            if (cac != null) {
                context = new CacheInvokeContext();
                context.cacheConfig = cac.getCacheConfig();
                enableCacheInContext = cac.isEnableCacheContext();
            }
        }
        if (context == null) {
            return method.invoke(src, args);
        } else {
            context.args = args;
            context.cacheProviderFactory = cacheProviderFactory;
            context.method = method;
            context.src = src;

            if (enableCacheInContext) {
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
            return getFromCache(context);
        } else {
            if (context.invoker == null) {
                return context.method.invoke(context.src, context.args);
            } else {
                return context.invoker.invoke();
            }
        }
    }

    private static Object getFromCache(CacheInvokeContext context)
            throws Throwable {
        CacheProvider cacheProvider = context.cacheProviderFactory.getCache(context.cacheConfig.getArea());
        String subArea = ClassUtil.getSubArea(context.cacheConfig, context.method);
        String key = cacheProvider.getKeyGenerator().getKey(context.args);
        GetCacheResult r = new GetCacheResult();
        if (context.cacheConfig.getCacheType() == CacheType.REMOTE) {
            CacheResult result = cacheProvider.getRemoteCache().get(context.cacheConfig, subArea, key);
            r.remoteResult = result.getResultCode();
            if (result.isSuccess()) {
                r.value = result.getValue();
            }
        } else {
            CacheResult result = cacheProvider.getLocalCache().get(context.cacheConfig, subArea, key);
            r.localResult = result.getResultCode();
            if (result.isSuccess()) {
                r.value = result.getValue();
            } else {
                if (context.cacheConfig.getCacheType() == CacheType.BOTH) {
                    result = cacheProvider.getRemoteCache().get(context.cacheConfig, subArea, key);
                    r.remoteResult = result.getResultCode();
                    if (result.isSuccess()) {
                        r.value = result.getValue();
                    }
                }
            }
        }
        if (context.cacheProviderFactory.getCacheMonitor() != null) {
            context.cacheProviderFactory.getCacheMonitor().onGet(context.cacheConfig, subArea, key, r.localResult, r.remoteResult);
        }

        boolean hit = r.localResult == CacheResultCode.SUCCESS || r.remoteResult == CacheResultCode.SUCCESS;

        if (!hit) {
            r.value = invoke(context.invoker, context.method, context.src, context.args);
            r.needUpdateLocal = r.localResult != null && (r.localResult == CacheResultCode.NOT_EXISTS || r.localResult == CacheResultCode.EXPIRED);
            r.needUpdateRemote = r.remoteResult != null && (r.remoteResult == CacheResultCode.NOT_EXISTS || r.remoteResult == CacheResultCode.EXPIRED);
        } else if (r.value == null && !context.cacheConfig.isCacheNullValue()) {
            r.value = invoke(context.invoker, context.method, context.src, context.args);
            r.needUpdateLocal = r.localResult != null;
            r.needUpdateRemote = r.remoteResult != null;
        } else {
            r.needUpdateLocal = r.localResult != null;
        }
        r.localResult = null;
        r.remoteResult = null;
        if (r.needUpdateLocal) {
            r.localResult = cacheProvider.getLocalCache().put(context.cacheConfig, subArea, key, r.value);
        }
        if (r.needUpdateRemote) {
            r.remoteResult = cacheProvider.getRemoteCache().put(context.cacheConfig, subArea, key, r.value);
        }
        if (context.cacheProviderFactory.getCacheMonitor() != null && (r.localResult != null || r.remoteResult != null)) {
            context.cacheProviderFactory.getCacheMonitor().onPut(context.cacheConfig, subArea, key, r.value, r.localResult, r.remoteResult);
        }
        return r.value;
    }

    private static Object invoke(Invoker invoker, Method method, Object src, Object[] args) throws Throwable {
        if (invoker == null) {
            return method.invoke(src, args);
        } else {
            return invoker.invoke();
        }
    }


}
