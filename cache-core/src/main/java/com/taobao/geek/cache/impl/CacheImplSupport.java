/**
 * Created on  13-09-12 09:51
 */
package com.taobao.geek.cache.impl;

import com.taobao.geek.cache.CacheConfig;
import com.taobao.geek.cache.CacheFactory;
import com.taobao.geek.cache.KeyGenerator;

import java.lang.reflect.Proxy;

/**
 * @author yeli.hl
 */
public class CacheImplSupport {

    public static KeyGenerator getDefaultKeyGenerator(){
        return new DefaultKeyGenerator();
    }

    public static <T> T enableCache(T target) {
        return CacheContextSupport.enableCache(target);
    }

    public static <T> T getProxy(T target, CacheConfig cacheConfig,CacheFactory cacheFactory) {
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CachedHandler h = new CachedHandler(target, cacheConfig, cacheFactory.getCache(cacheConfig.getArea()));
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }
}
