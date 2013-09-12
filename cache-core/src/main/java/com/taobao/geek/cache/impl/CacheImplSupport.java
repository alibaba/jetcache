/**
 * Created on  13-09-12 09:51
 */
package com.taobao.geek.cache.impl;

import com.taobao.geek.cache.CacheConfig;
import com.taobao.geek.cache.CacheFactory;
import com.taobao.geek.cache.Cached;
import com.taobao.geek.cache.KeyGenerator;

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

    public static <T> T getProxy(T target, CacheConfig cacheConfig, CacheFactory cacheFactory) {
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CachedHandler h = new CachedHandler(target, cacheConfig, cacheFactory);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    public static <T> T getProxyByAnnotation(T target, CacheFactory cacheFactory) {
        final HashMap<String, CacheConfig> configMap = new HashMap<String, CacheConfig>();
        processType(configMap, target.getClass());
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CachedHandler h = new CachedHandler(target, configMap, cacheFactory);
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
            CacheConfig cc = parseCacheConfig(m);
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

    private static CacheConfig parseCacheConfig(Method m) {
        Cached anno = m.getAnnotation(Cached.class);
        if (anno == null) {
            return null;
        }
        CacheConfig cc = new CacheConfig();
        cc.setArea(anno.area());
        cc.setCacheType(anno.cacheType());
        cc.setEnabled(anno.enabled());
        cc.setExpire(anno.expire());
        cc.setLocalLimit(anno.localLimit());
        cc.setVersion(anno.version());
        return cc;
    }
}
