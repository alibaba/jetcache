/**
 * Created on  13-09-19 20:56
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.impl.CacheAnnoConfig;
import com.taobao.geek.jetcache.impl.CacheConfigUtil;
import com.taobao.geek.jetcache.impl.CacheImplSupport;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CachePointcut extends StaticMethodMatcherPointcut implements ClassFilter {

    private IdentityHashMap<Method, CacheAnnoConfig> cacheConfigMap = new IdentityHashMap<Method, CacheAnnoConfig>();

    public CachePointcut() {
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

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        CacheAnnoConfig cac = cacheConfigMap.get(method);
        if (cac == CacheAnnoConfig.getNoCacheAnnoConfigInstance()) {
            return false;
        } else if (cac != null) {
            return true;
        } else {
            cac = new CacheAnnoConfig();
            CacheConfigUtil.parse(cac, method);

            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            parseByTargetClass(cac, targetClass, name, paramTypes);

            if (!cac.isEnableCacheContext() && cac.getCacheConfig() == null) {
                cacheConfigMap.put(method, CacheAnnoConfig.getNoCacheAnnoConfigInstance());
                return false;
            } else {
                cacheConfigMap.put(method, cac);
                return true;
            }
        }
    }

    private void parseByTargetClass(CacheAnnoConfig cac, Class<?> clazz, String name, Class<?>[] paramTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, paramTypes);
            CacheConfigUtil.parse(cac, method);
        } catch (NoSuchMethodException e) {
            //TODO optimize it
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
