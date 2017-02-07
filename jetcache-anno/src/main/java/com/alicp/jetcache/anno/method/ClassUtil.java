/**
 * Created on  13-09-09 17:20
 */
package com.alicp.jetcache.anno.method;

import org.springframework.asm.Type;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ClassUtil {

    private static ConcurrentHashMap<Method, String> cacheNameMap = new ConcurrentHashMap();
    private static ConcurrentHashMap<Method, String> methodSigMap = new ConcurrentHashMap();

    public static String generateCacheName(Method method, String[] hiddenPackages) {
        String prefix = cacheNameMap.get(method);

        if (prefix == null) {
            final StringBuilder sb = new StringBuilder();
            String className = method.getDeclaringClass().getName();
            sb.append(getShortClassName(removeHiddenPackage(hiddenPackages, className)));
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
        } else {
            return prefix;
        }
    }

    private static void getDescriptor(final StringBuilder sb, final Class<?> c, String[] hiddenPackages) {
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
                name = getShortClassName(name);
                sb.append(name);
                sb.append(';');
                return;
            }
        }
    }


    public static String getShortClassName(String className) {
        if (className == null) {
            return null;
        }
        String[] ss = className.split("\\.");
        StringBuilder sb = new StringBuilder(className.length());
        for (int i = 0; i < ss.length; i++) {
            String s = ss[i];
            if (i != ss.length - 1) {
                sb.append(s.charAt(0)).append('.');
            } else {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static String removeHiddenPackage(String[] hiddenPackages, String packageOrFullClassName) {
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


    public static Class<?>[] getAllInterfaces(Object obj) {
        Class<?> c = obj.getClass();
        HashSet<Class<?>> s = new HashSet<>();
        do {
            Class<?>[] its = c.getInterfaces();
            Collections.addAll(s, its);
            c = c.getSuperclass();
        } while (c != null);
        return s.toArray(new Class<?>[s.size()]);
    }

    private static void getMethodSig(StringBuilder sb, Method m) {
        sb.append(m.getName());
        sb.append(Type.getType(m).getDescriptor());
    }

    public static String getMethodSig(Method m) {
        String sig = methodSigMap.get(m);
        if (sig != null) {
            return sig;
        } else {
            StringBuilder sb = new StringBuilder();
            getMethodSig(sb, m);
            sig = sb.toString();
            methodSigMap.put(m, sig);
            return sig;
        }
    }
}
