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
    private CacheWapper cacheWapper;

    public CachedHandler(CacheConfig cacheConfig, CacheWapper cacheWapper) {
        this.cacheConfig = cacheConfig;
        this.cacheWapper = cacheWapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
