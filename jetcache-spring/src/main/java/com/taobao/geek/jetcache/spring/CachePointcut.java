/**
 * Created on  13-09-19 20:56
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.impl.CacheInvokeConfig;
import com.taobao.geek.jetcache.impl.CacheConfigUtil;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CachePointcut extends StaticMethodMatcherPointcut implements ClassFilter {

    private IdentityHashMap<Method, CacheInvokeConfig> cacheConfigMap = new IdentityHashMap<Method, CacheInvokeConfig>();

    public CachePointcut() {
        setClassFilter(this);
    }

    @Override
    public boolean matches(Class<?> clazz) {
        if (exclude(clazz)) {
            return false;
        }
        return true;
    }

    public boolean exclude(Class<?> clazz) {
        if (clazz.getName().startsWith("java")) {
            return true;
        }
        if (clazz.getName().startsWith("org.springframework")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        CacheInvokeConfig cac = cacheConfigMap.get(method);
        if (cac == CacheInvokeConfig.getNoCacheInvokeConfigInstance()) {
            return false;
        } else if (cac != null) {
            return true;
        } else {
            cac = new CacheInvokeConfig();
            CacheConfigUtil.parse(cac, method);

            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            parseByTargetClass(cac, targetClass, name, paramTypes);

            if (!cac.isEnableCacheContext() && cac.getCacheConfig() == null) {
                cacheConfigMap.put(method, CacheInvokeConfig.getNoCacheInvokeConfigInstance());
                return false;
            } else {
                cacheConfigMap.put(method, cac);
                return true;
            }
        }
    }

    private void parseByTargetClass(CacheInvokeConfig cac, Class<?> clazz, String name, Class<?>[] paramTypes) {
        if (exclude(clazz)) {
            return;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (methodMatch(name, method, paramTypes)) {
                CacheConfigUtil.parse(cac, method);
                break;
            }
        }

        if (!clazz.isInterface() && clazz.getSuperclass() != null) {
            parseByTargetClass(cac, clazz.getSuperclass(), name, paramTypes);
        }
        Class<?>[] intfs = clazz.getInterfaces();
        for (Class<?> it : intfs) {
            parseByTargetClass(cac, it, name, paramTypes);
        }
    }

    private boolean methodMatch(String name, Method method, Class<?>[] paramTypes) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (!name.equals(method.getName())) {
            return false;
        }
        Class<?>[] ps = method.getParameterTypes();
        if (ps.length != paramTypes.length) {
            return false;
        }
        for (int i = 0; i < ps.length; i++) {
            if (!ps[i].equals(paramTypes[i])) {
                return false;
            }
        }
        return true;
    }


    public void setCacheConfigMap(IdentityHashMap cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }
}
