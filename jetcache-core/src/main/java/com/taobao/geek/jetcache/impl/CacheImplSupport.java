/**
 * Created on  13-09-12 09:51
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * @author yeli.hl
 */
public class CacheImplSupport {

    public static KeyGenerator getDefaultKeyGenerator() {
        return new DefaultKeyGenerator();
    }

    public static <T> T enableCache(T target) {
        return CacheContextSupport.enableCache(target);
    }

    public static void enableCache(Callback callback) throws Exception{
        CacheContextSupport.enableCache(callback);
    }

    public static <T> T enableCache(ReturnValueCallback<T> callback) throws Exception {
        return CacheContextSupport.enableCache(callback);
    }

    public static <T> T getProxy(T target, CacheConfig cacheConfig, CacheProviderFactory cacheProviderFactory) {
        return ProxyUtil.getProxy(target, cacheConfig, cacheProviderFactory);
    }

    public static <T> T getProxyByAnnotation(T target, CacheProviderFactory cacheProviderFactory) {
        return ProxyUtil.getProxyByAnnotation(target, cacheProviderFactory);
    }

    public static CacheConfig parseCacheConfig(Method m) {
        return CacheConfigUtil.parseCacheConfig(m);
    }

    public static boolean parseEnableCacheConfig(Method m){
        return CacheConfigUtil.parseEnableCacheConfig(m);
    }

    public static Object invoke(Object src, Method method, Object[] args, CacheProviderFactory cacheProviderFactory,
                                CacheConfig cc) throws Throwable {
        return CachedHandler.invoke(src, method, args, cacheProviderFactory, cc);
    }
}
