/**
 * Created on  13-09-20 21:36
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class ProxyUtil {
    public static <T> T getProxy(T target, CacheConfig cacheConfig, CacheProviderFactory cacheProviderFactory) {
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CacheHandler h = new CacheHandler(target, cacheConfig, cacheProviderFactory);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    public static <T> T getProxyByAnnotation(T target, CacheProviderFactory cacheProviderFactory) {
        final HashMap<String, CacheAnnoConfig> configMap = new HashMap<String, CacheAnnoConfig>();
        processType(configMap, target.getClass());
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CacheHandler h = new CacheHandler(target, configMap, cacheProviderFactory);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    private static void processType(HashMap<String, CacheAnnoConfig> configMap, Class<?> clazz) {
        if (clazz.isAnnotation() || clazz.isArray() || clazz.isEnum() || clazz.isPrimitive()) {
            throw new IllegalArgumentException(clazz.getName());
        }
        if (clazz.getName().startsWith("java")) {
            return;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (Modifier.isPublic(m.getModifiers())) {
                processMethod(configMap, m);
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

    private static void processMethod(HashMap<String, CacheAnnoConfig> configMap, Method m) {
        String sig = ClassUtil.getMethodSig(m);
        CacheAnnoConfig cac = configMap.get(sig);
        if (cac == null) {
            CacheConfig cc = CacheConfigUtil.parseCacheConfig(m);
            boolean enable = CacheConfigUtil.parseEnableCacheConfig(m);
            if (cc != null || enable) {
                cac = new CacheAnnoConfig();
                cac.setCacheConfig(cc);
                cac.setEnableCacheContext(enable);
                configMap.put(sig, cac);
            }
        } else {
            CacheConfigUtil.parse(cac, m);
        }
    }
}
