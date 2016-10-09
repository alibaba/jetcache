/**
 * Created on  13-09-20 21:36
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ProxyUtil {
    public static <T> T getProxy(T target, CacheAnnoConfig cacheAnnoConfig, GlobalCacheConfig globalCacheConfig) {
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CacheHandler h = new CacheHandler(target, cacheAnnoConfig, globalCacheConfig);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    public static <T> T getProxyByAnnotation(T target, GlobalCacheConfig globalCacheConfig) {
        final HashMap<String, CacheInvokeConfig> configMap = new HashMap<String, CacheInvokeConfig>();
        processType(configMap, target.getClass(), globalCacheConfig);
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CacheHandler h = new CacheHandler(target, configMap, globalCacheConfig);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    private static void processType(HashMap<String, CacheInvokeConfig> configMap, Class<?> clazz, GlobalCacheConfig gcc) {
        if (clazz.isAnnotation() || clazz.isArray() || clazz.isEnum() || clazz.isPrimitive()) {
            throw new IllegalArgumentException(clazz.getName());
        }
        if (clazz.getName().startsWith("java")) {
            return;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (Modifier.isPublic(m.getModifiers())) {
                processMethod(configMap, m, gcc);
            }
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> it : interfaces) {
            processType(configMap, it, gcc);
        }

        if (!clazz.isInterface()) {
            if (clazz.getSuperclass() != null) {
                processType(configMap, clazz.getSuperclass(), gcc);
            }
        }
    }

    private static void processMethod(HashMap<String, CacheInvokeConfig> configMap, Method m, GlobalCacheConfig gcc) {
        String sig = ClassUtil.getMethodSig(m, gcc.getHidePackages());
        CacheInvokeConfig cac = configMap.get(sig);
        if (cac == null) {
            cac = new CacheInvokeConfig();
            if (CacheConfigUtil.parse(cac, m)) {
                configMap.put(sig, cac);
            }
        } else {
            CacheConfigUtil.parse(cac, m);
        }
    }
}
