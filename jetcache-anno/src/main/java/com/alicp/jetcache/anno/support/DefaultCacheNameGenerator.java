/**
 * Created on 2018/3/22.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.method.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultCacheNameGenerator implements CacheNameGenerator {

    private String[] hiddenPackages;

    public DefaultCacheNameGenerator(String[] hiddenPackages) {
        this.hiddenPackages = hiddenPackages;
    }

    @Override
    public String generateCacheName(Method method) {
        return ClassUtil.generateCacheName(method, hiddenPackages);
    }

    @Override
    public String generateCacheName(Field field) {
        StringBuilder sb = new StringBuilder();
        String className = field.getDeclaringClass().getName();
        className = ClassUtil.removeHiddenPackage(hiddenPackages, className);
        className = ClassUtil.getShortClassName(className);
        sb.append(className);
        sb.append(".").append(field.getName());
        return sb.toString();
    }
}
