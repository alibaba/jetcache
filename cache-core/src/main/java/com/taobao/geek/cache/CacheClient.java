/**
 * Created on  13-09-04 14:03
 */
package com.taobao.geek.cache;

import java.lang.reflect.Proxy;

/**
 * @author yeli.hl
 */
public class CacheClient {

    private CacheFactory cacheFactory;

    public <T> T getProxy(T target, CacheConfig cacheConfig) {
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CachedHandler h = new CachedHandler(cacheConfig, cacheFactory.getCache(cacheConfig.getArea()));
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    public <T> T getProxyByAnnotation(T target) {
        return null;
    }

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }
}
