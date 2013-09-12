/**
 * Created on  13-09-09 15:59
 */
package com.taobao.geek.cache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
class CachedHandler implements InvocationHandler {

    private Object src;
    private CacheConfig cacheConfig;
    private CacheProvider cacheProvider;

    private Cache localCache;
    private Cache remoteCache;

    private static class GetCacheResult {
        boolean needUpdateLocal = false;
        boolean needUpdateRemote = false;
        Object value = null;
    }


    public CachedHandler(Object src, CacheConfig cacheConfig, CacheProvider cacheProvider) {
        this.src = src;
        this.cacheConfig = cacheConfig;
        this.cacheProvider = cacheProvider;
        this.localCache = cacheProvider.getLocalCache();
        this.remoteCache = cacheProvider.getRemoteCache();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (cacheConfig.isEnabled() || CacheContext.isEnabled()) {
            String key = cacheProvider.getKeyGenerator().getKey(method, args, cacheConfig.getVersion());
            GetCacheResult r = getFromCache(key);
            if (r.value != null) {
                if (r.needUpdateLocal) {
                    localCache.put(key, r.value);
                }
                return r.value;
            } else {
                Object value = method.invoke(src, args);
                if (r.needUpdateLocal) {
                    localCache.put(key, value);
                }
                if (r.needUpdateRemote) {
                    remoteCache.put(key, value);
                }
                return value;
            }
        } else {
            return method.invoke(src, args);
        }
    }

    private GetCacheResult getFromCache(String key) {
        GetCacheResult r = new GetCacheResult();
        if (cacheConfig.getCacheType() == CacheType.REMOTE) {
            CacheResult result = remoteCache.get(key);
            if (result.isSuccess()) {
                r.value = result.getValue();
            } else {
                r.needUpdateRemote = true;
            }
        } else {
            CacheResult result = localCache.get(key);
            if (result.isSuccess()) {
                r.value = result.getValue();
            } else {
                r.needUpdateLocal = true;
                if (cacheConfig.getCacheType() == CacheType.BOTH) {
                    result = remoteCache.get(key);
                    if (result.isSuccess()) {
                        r.value = result.getValue();
                    } else {
                        r.needUpdateRemote = true;
                    }
                }
            }
        }
        return r;
    }
}
