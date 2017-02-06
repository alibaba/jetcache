/**
 * Created on  13-09-09 17:20
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.CacheConsts;
import org.springframework.asm.Type;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ClassUtil {

    private static ConcurrentHashMap<Method, String> subAreaMap = new ConcurrentHashMap();
    private static ConcurrentHashMap<Method, String> methodSigMap = new ConcurrentHashMap();

    public static String generateCacheName(Method method, String[] hiddenPackages) {
        // TODO invalid cache when param type changed
        String prefix = subAreaMap.get(method);

        if (prefix == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getDeclaringClass().getName());
            sb.append('.');
            getMethodSig(sb, method);
            String str = removeHiddenPackage(hiddenPackages, sb);
            subAreaMap.put(method, str);
            return str;
        } else {
            return prefix;
        }
    }

    public static String removeHiddenPackage(String[] hiddenPackages, StringBuilder sb) {
        String str = sb.toString();
        if (hiddenPackages != null) {
            for (String p : hiddenPackages) {
                String pWithDot = p + ".";
                str = str.replace(pWithDot, "");
                str = str.replace(pWithDot.replace('.', '/'), "");
            }
        }
        return str;
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
