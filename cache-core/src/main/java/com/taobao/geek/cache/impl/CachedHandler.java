/**
 * Created on  13-09-09 15:59
 */
package com.taobao.geek.cache.impl;

import com.taobao.geek.cache.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author yeli.hl
 */
class CachedHandler implements InvocationHandler {

    private Object src;
    private CacheFactory cacheFactory;

    // 下面两个是二选一的
    private CacheConfig cacheConfig;
    private HashMap<String, CacheConfig> configMap;

    private static class GetCacheResult {
        boolean needUpdateLocal = false;
        boolean needUpdateRemote = false;
        Object value = null;
    }

    public CachedHandler(Object src, CacheConfig cacheConfig, CacheFactory cacheFactory) {
        this.src = src;
        this.cacheConfig = cacheConfig;
        this.cacheFactory = cacheFactory;
    }

    public CachedHandler(Object src, HashMap<String, CacheConfig> configMap, CacheFactory cacheFactory) {
        this.src = src;
        this.configMap = configMap;
        this.cacheFactory = cacheFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CacheConfig cc = cacheConfig;
        if (cc == null) {
            String sig = ClassUtil.getMethodSig(method);
            cc = configMap.get(sig);
        }
        if (cc == null) {
            return method.invoke(src, args);
        }

        if (cc.isEnabled() || CacheContextSupport.isEnabled()) {
            CacheProvider cacheProvider = cacheFactory.getCache(cc.getArea());
            String key = cacheProvider.getKeyGenerator().getKey(method, args, cc.getVersion());
            Cache localCache = cacheProvider.getLocalCache();
            Cache remoteCache = cacheProvider.getRemoteCache();
            GetCacheResult r = getFromCache(cacheConfig.getCacheType(), key, localCache, remoteCache);
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

    private static GetCacheResult getFromCache(CacheType cacheType,String key, Cache localCache, Cache remoteCache) {
        GetCacheResult r = new GetCacheResult();
        if (cacheType == CacheType.REMOTE) {
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
                if (cacheType == CacheType.BOTH) {
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
