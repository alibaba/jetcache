/**
 * Created on  13-09-09 15:59
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author yeli.hl
 */
class CachedHandler implements InvocationHandler {

    private Object src;
    private CacheProviderFactory cacheProviderFactory;

    // 下面两个是二选一的
    private CacheConfig cacheConfig;
    private HashMap<String, CacheAnnoConfig> configMap;

    private static class GetCacheResult {
        boolean needUpdateLocal = false;
        boolean needUpdateRemote = false;
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
        CacheConfig cc = cacheConfig;
        final CacheAnnoConfig cac;
        if (cc != null) {
            cac = new CacheAnnoConfig();
            cac.setCacheConfig(cc);
        } else {
            String sig = ClassUtil.getMethodSig(method);
            cac = configMap.get(sig);
        }
        if (cac == null) {
            return method.invoke(src, args);
        } else{
            if(cac.isEnableCacheContext()){
                return CacheContextSupport.enableCache(new ReturnValueCallback<Object>() {
                    @Override
                    public Object execute() throws Exception {
                        try {
                            return invoke(src, method, args, cacheProviderFactory, cac.getCacheConfig());
                        } catch (Throwable e) {
                            if (e instanceof Exception) {
                                throw (Exception) e;
                            } else if (e instanceof Error) {
                                throw (Error) e;
                            } else {
                                throw new CacheException("", e);
                            }
                        }
                    }
                });
            } else {
                return invoke(src, method, args, cacheProviderFactory, cc);
            }
        }

    }

    public static Object invoke(Object src, Method method, Object[] args, CacheProviderFactory cacheProviderFactory,
                                 CacheConfig cc) throws Throwable {
        if (cc.isEnabled() || CacheContextSupport.isEnabled()) {
            return getFromCache(src, method, args, cacheProviderFactory, cc);
        } else {
            return method.invoke(src, args);
        }
    }

    private static Object getFromCache(Object src, Method method, Object[] args, CacheProviderFactory cacheProviderFactory,
                                       CacheConfig cc)
            throws IllegalAccessException, InvocationTargetException {
        CacheProvider cacheProvider = cacheProviderFactory.getCache(cc.getArea());
        String subArea = SubAreaUtil.getSubArea(cc, method);
        String key = cacheProvider.getKeyGenerator().getKey(cc, method, args);
        Cache localCache = cacheProvider.getLocalCache();
        Cache remoteCache = cacheProvider.getRemoteCache();
        GetCacheResult r = new GetCacheResult();
        if (cc.getCacheType() == CacheType.REMOTE) {
            CacheResult result = remoteCache.get(cc, subArea, key);
            if (result.isSuccess()) {
                r.value = result.getValue();
            } else {
                r.needUpdateRemote = true;
            }
        } else {
            CacheResult result = localCache.get(cc, subArea, key);
            if (result.isSuccess()) {
                r.value = result.getValue();
            } else {
                r.needUpdateLocal = true;
                if (cc.getCacheType() == CacheType.BOTH) {
                    result = remoteCache.get(cc, subArea, key);
                    if (result.isSuccess()) {
                        r.value = result.getValue();
                    } else {
                        r.needUpdateRemote = true;
                    }
                }
            }
        }


        if (r.value != null) {
            if (r.needUpdateLocal) {
                localCache.put(cc, subArea, key, r.value);
            }
            return r.value;
        } else {
            Object value = method.invoke(src, args);
            if (r.needUpdateLocal) {
                localCache.put(cc, subArea, key, value);
            }
            if (r.needUpdateRemote) {
                remoteCache.put(cc, subArea, key, value);
            }
            return value;
        }
    }

}
