/**
 * Created on  13-09-19 20:56
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.impl.CacheAnnoConfig;
import com.taobao.geek.jetcache.impl.CacheImplSupport;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public class JetCachePointcut extends StaticMethodMatcherPointcut implements ClassFilter {

    private IdentityHashMap<Method, CacheAnnoConfig> cacheConfigMap = new IdentityHashMap<Method, CacheAnnoConfig>();

    public JetCachePointcut() {
        setClassFilter(this);
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
        CacheAnnoConfig cac = cacheConfigMap.get(method);
        if (cac == CacheAnnoConfig.getNoCacheAnnoConfigInstance()) {
            return false;
        } else if (cac != null) {
            return true;
        } else {
            cac = new CacheAnnoConfig();
            parse(cac, method);

            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            parseByTargetClass(cac, targetClass, name, paramTypes);

            if (!cac.isEnableCache() && cac.getCacheConfig() == null) {
                cacheConfigMap.put(method, CacheAnnoConfig.getNoCacheAnnoConfigInstance());
                return false;
            } else {
                cacheConfigMap.put(method, cac);
                return true;
            }
        }
    }

    private void parse(CacheAnnoConfig cac, Method method) {
        CacheConfig cc = CacheImplSupport.parseCacheConfig(method);
        if (cc != null) {
            cac.setCacheConfig(cc);
        }
        boolean enable = CacheImplSupport.parseEnableCacheConfig(method);
        if (enable) {
            cac.setEnableCache(true);
        }
    }

    private void parseByTargetClass(CacheAnnoConfig cac, Class<?> clazz, String name, Class<?>[] paramTypes) {
        try {
            Method method = clazz.getMethod(name, paramTypes);
            parse(cac, method);
        } catch (NoSuchMethodException e) {
            //TODO 这样效率太低
        }
        if (!clazz.isInterface() && clazz.getSuperclass() != null) {
            parseByTargetClass(cac, clazz.getSuperclass(), name, paramTypes);
        }
        Class<?>[] intfs = clazz.getInterfaces();
        for (Class<?> it : intfs) {
            parseByTargetClass(cac, it, name, paramTypes);
        }
    }

    public void setCacheConfigMap(IdentityHashMap cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }
}
