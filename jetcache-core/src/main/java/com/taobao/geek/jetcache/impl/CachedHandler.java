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
        final CacheAnnoConfig cac;
        if (cacheConfig != null) {
            cac = new CacheAnnoConfig();
            cac.setCacheConfig(cacheConfig);
        } else {
            String sig = ClassUtil.getMethodSig(method);
            cac = configMap.get(sig);
        }
        if (cac == null) {
            return method.invoke(src, args);
        } else {
            if (cac.isEnableCacheContext()) {
                try {
                    CacheContextSupport.enable();
                    return invoke(null, src, method, args, cacheProviderFactory, cac.getCacheConfig());
                } finally {
                    CacheContextSupport.disable();
                }
            } else {
                return invoke(null, src, method, args, cacheProviderFactory, cac.getCacheConfig());
            }
        }
    }

    public static Object invoke(Invoker invoker, Object src, Method method, Object[] args, CacheProviderFactory cacheProviderFactory,
                                CacheConfig cc) throws Throwable {
        if (cc != null && (cc.isEnabled() || CacheContextSupport.isEnabled())) {
            return getFromCache(invoker, src, method, args, cacheProviderFactory, cc);
        } else {
            if (invoker == null) {
                return method.invoke(src, args);
            } else {
                return invoker.invoke();
            }
        }
    }

    private static Object getFromCache(Invoker invoker, Object src, Method method, Object[] args,
                                       CacheProviderFactory cacheProviderFactory, CacheConfig cc)
            throws Throwable {
        CacheProvider cacheProvider = cacheProviderFactory.getCache(cc.getArea());
        String subArea = ClassUtil.getSubArea(cc, method);
        String key = cacheProvider.getKeyGenerator().getKey(args);
        GetCacheResult r = new GetCacheResult();
        if (cc.getCacheType() == CacheType.REMOTE) {
            CacheResult result = cacheProvider.getRemoteCache().get(cc, subArea, key);
            r.remoteResult = result.getResultCode();
            if (result.isSuccess()) {
                r.value = result.getValue();
            } else {
                r.needUpdateRemote = true;
            }
        } else {
            CacheResult result = cacheProvider.getLocalCache().get(cc, subArea, key);
            r.localResult = result.getResultCode();
            if (result.isSuccess()) {
                r.value = result.getValue();
            } else {
                r.needUpdateLocal = true;
                if (cc.getCacheType() == CacheType.BOTH) {
                    result = cacheProvider.getRemoteCache().get(cc, subArea, key);
                    r.remoteResult = result.getResultCode();
                    if (result.isSuccess()) {
                        r.value = result.getValue();
                    } else {
                        r.needUpdateRemote = true;
                    }
                }
            }
        }
        if (cacheProviderFactory.getCacheMonitor() != null) {
            cacheProviderFactory.getCacheMonitor().onGet(cc, subArea, key, r.localResult, r.remoteResult);
        }

        r.localResult = null;
        r.remoteResult = null;
        if (r.value != null) {
            if (r.needUpdateLocal) {
                r.localResult = cacheProvider.getLocalCache().put(cc, subArea, key, r.value);
            }
        } else {
            if (invoker == null) {
                r.value = method.invoke(src, args);
            } else {
                r.value = invoker.invoke();
            }
            if (r.needUpdateLocal) {
                r.localResult = cacheProvider.getLocalCache().put(cc, subArea, key, r.value);
            }
            if (r.needUpdateRemote) {
                r.remoteResult = cacheProvider.getRemoteCache().put(cc, subArea, key, r.value);
            }
        }
        if (cacheProviderFactory.getCacheMonitor() != null) {
            cacheProviderFactory.getCacheMonitor().onPut(cc, subArea, key, r.value, r.localResult, r.remoteResult);
        }
        return r.value;
    }

}
