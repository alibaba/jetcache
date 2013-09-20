/**
 * Created on  13-09-20 21:36
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;
import com.taobao.geek.jetcache.Cached;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * @author yeli.hl
 */
class ProxyUtil {
    public static <T> T getProxy(T target, CacheConfig cacheConfig, CacheProviderFactory cacheProviderFactory) {
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CachedHandler h = new CachedHandler(target, cacheConfig, cacheProviderFactory);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    public static <T> T getProxyByAnnotation(T target, CacheProviderFactory cacheProviderFactory) {
        final HashMap<String, CacheConfig> configMap = new HashMap<String, CacheConfig>();
        processType(configMap, target.getClass());
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CachedHandler h = new CachedHandler(target, configMap, cacheProviderFactory);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    private static void processType(HashMap<String, CacheConfig> configMap, Class<?> clazz) {
        if (clazz.isAnnotation() || clazz.isArray() || clazz.isEnum() || clazz.isPrimitive()) {
            throw new IllegalArgumentException(clazz.getName());
        }
        if (clazz.getName().startsWith("java")) {
            return;
        }
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            CacheConfig cc = CacheConfigUtil.parseCacheConfig(m);
            if (cc != null) {
                configMap.put(ClassUtil.getMethodSig(m), cc);
            }
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> it : interfaces) {
            processType(configMap, it);
        }
        if (!clazz.isInterface()) {
            if (clazz.getSuperclass() != null) {
                processType(configMap, clazz.getSuperclass());
            }
        }
    }
}
