/**
 * Created on  13-09-19 20:56
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.impl.CacheImplSupport;
import com.taobao.geek.jetcache.impl.NoCacheConfig;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public class JetCachePointcut extends StaticMethodMatcherPointcut implements ClassFilter {

    private IdentityHashMap<Method, CacheConfig> configMap = new IdentityHashMap<Method, CacheConfig>();

    public JetCachePointcut() {
        //setClassFilter(this);
    }

    @Override
    public boolean matches(Class<?> clazz) {
        if (clazz.getName().startsWith("java")) {
            return false;
        }
        if (clazz.getName().startsWith("org.springframework")) {
            return false;
        }
        return true;
    }

    public boolean matches(Method method, Class<?> targetClass) {
        CacheConfig cc = configMap.get(method);
        if (cc == NoCacheConfig.instance()) {
            return false;
        } else if (cc != null) {
            return true;
        } else {
            cc = CacheImplSupport.parseCacheConfig(method);
            if (cc != null) {
                configMap.put(method, cc);
                return true;
            } else {
                String name = method.getName();
                Class<?>[] paramTypes = method.getParameterTypes();
                cc = getByTargetClass(targetClass, name, paramTypes);
                if (cc != null) {
                    configMap.put(method, cc);
                    return true;
                } else {
                    configMap.put(method, NoCacheConfig.instance());
                    return false;
                }
            }
        }
    }

    private CacheConfig getByTargetClass(Class<?> clazz, String name, Class<?>[] paramTypes) {
        try {
            Method method = clazz.getMethod(name, paramTypes);
            CacheConfig cc = CacheImplSupport.parseCacheConfig(method);
            if (cc != null) {
                return cc;
            }
        } catch (NoSuchMethodException e) {
        }
        if (!clazz.isInterface() && clazz.getSuperclass() != null) {
            CacheConfig cc = getByTargetClass(clazz.getSuperclass(), name, paramTypes);
            if (cc != null) {
                return cc;
            }
        }
        Class<?>[] intfs = clazz.getInterfaces();
        for (Class<?> it : intfs) {
            CacheConfig cc = getByTargetClass(it, name, paramTypes);
            if (cc != null) {
                return cc;
            }
        }
        return null;
    }

    public void setConfigMap(IdentityHashMap configMap) {
        this.configMap = configMap;
    }
}
