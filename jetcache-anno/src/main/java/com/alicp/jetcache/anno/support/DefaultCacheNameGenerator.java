/**
 * Created on 2018/3/22.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.method.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultCacheNameGenerator implements CacheNameGenerator {

    protected final String[] hiddenPackages;

    protected final ConcurrentHashMap<Method, String> cacheNameMap = new ConcurrentHashMap();

    public DefaultCacheNameGenerator(String[] hiddenPackages) {
        this.hiddenPackages = hiddenPackages;
    }

    @Override
    public String generateCacheName(Method method, Object targetObject) {
        String cacheName = cacheNameMap.get(method);

        if (cacheName == null) {
            final StringBuilder sb = new StringBuilder();

            String className = method.getDeclaringClass().getName();
            sb.append(ClassUtil.getShortClassName(removeHiddenPackage(hiddenPackages, className)));
            sb.append('.');
            sb.append(method.getName());
            sb.append('(');

            for(Class<?> c : method.getParameterTypes()){
                getDescriptor(sb, c , hiddenPackages);
            }

            sb.append(')');

            String str = sb.toString();
            cacheNameMap.put(method, str);
            return str;
        }

        return cacheName;
    }

    @Override
    public String generateCacheName(Field field) {
        StringBuilder sb = new StringBuilder();
        String className = field.getDeclaringClass().getName();
        className = removeHiddenPackage(hiddenPackages, className);
        className = ClassUtil.getShortClassName(className);
        sb.append(className);
        sb.append(".").append(field.getName());
        return sb.toString();
    }

    protected String removeHiddenPackage(String[] hiddenPackages, String packageOrFullClassName) {
        if (hiddenPackages != null && packageOrFullClassName != null) {
            for (String p : hiddenPackages) {
                if (p != null && packageOrFullClassName.startsWith(p)) {
                    packageOrFullClassName = Pattern.compile(p, Pattern.LITERAL).matcher(
                            packageOrFullClassName).replaceFirst("");
                    if (packageOrFullClassName.length() > 0 && packageOrFullClassName.charAt(0) == '.') {
                        packageOrFullClassName = packageOrFullClassName.substring(1);
                    }
                    return packageOrFullClassName;
                }
            }
        }
        return packageOrFullClassName;
    }

    protected void getDescriptor(final StringBuilder sb, final Class<?> c, String[] hiddenPackages) {
        Class<?> d = c;
        while (true) {
            if (d.isPrimitive()) {
                char car;
                if (d == Integer.TYPE) {
                    car = 'I';
                } else if (d == Void.TYPE) {
                    car = 'V';
                } else if (d == Boolean.TYPE) {
                    car = 'Z';
                } else if (d == Byte.TYPE) {
                    car = 'B';
                } else if (d == Character.TYPE) {
                    car = 'C';
                } else if (d == Short.TYPE) {
                    car = 'S';
                } else if (d == Double.TYPE) {
                    car = 'D';
                } else if (d == Float.TYPE) {
                    car = 'F';
                } else /* if (d == Long.TYPE) */{
                    car = 'J';
                }
                sb.append(car);
                return;
            } else if (d.isArray()) {
                sb.append('[');
                d = d.getComponentType();
            } else {
                sb.append('L');
                String name = d.getName();
                name = removeHiddenPackage(hiddenPackages, name);
                name = ClassUtil.getShortClassName(name);
                sb.append(name);
                sb.append(';');
                return;
            }
        }
    }
}
