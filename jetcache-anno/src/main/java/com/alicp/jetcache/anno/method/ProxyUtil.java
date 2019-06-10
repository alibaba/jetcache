/**
 * Created on  13-09-20 21:36
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.support.ConfigMap;
import com.alicp.jetcache.anno.support.ConfigProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ProxyUtil {

    public static <T> T getProxyByAnnotation(T target, ConfigProvider configProvider) {
        final ConfigMap configMap = new ConfigMap();
        processType(configMap, target.getClass());
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        CacheHandler h = new CacheHandler(target, configMap,
                () -> configProvider.getCacheContext().createCacheInvokeContext(configMap),
                configProvider.getGlobalCacheConfig().getHiddenPackages());
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, h);
        return (T) o;
    }

    private static void processType(ConfigMap configMap, Class<?> clazz) {
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

    private static void processMethod(ConfigMap configMap, Method m) {
        String sig = ClassUtil.getMethodSig(m);
        CacheInvokeConfig cac = configMap.getByMethodInfo(sig);
        if (cac == null) {
            cac = new CacheInvokeConfig();
            if (CacheConfigUtil.parse(cac, m)) {
                configMap.putByMethodInfo(sig, cac);
            }
        } else {
            CacheConfigUtil.parse(cac, m);
        }
    }
}
