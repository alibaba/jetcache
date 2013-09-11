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

    private CacheConfig cacheConfig;
    private CacheProvider cacheProvider;

    public CachedHandler(CacheConfig cacheConfig, CacheProvider cacheProvider) {
        this.cacheConfig = cacheConfig;
        this.cacheProvider = cacheProvider;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
