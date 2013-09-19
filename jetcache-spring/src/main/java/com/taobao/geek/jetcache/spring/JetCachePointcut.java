/**
 * Created on  13-09-19 20:56
 */
package com.taobao.geek.jetcache.spring;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public class JetCachePointcut extends StaticMethodMatcherPointcut implements ClassFilter {

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
        System.out.println("matches invoked");
        return true;
    }
}
