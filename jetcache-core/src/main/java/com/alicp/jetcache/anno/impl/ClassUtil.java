/**
 * Created on  13-09-09 17:20
 */
package com.alicp.jetcache.anno.impl;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.alicp.jetcache.support.CacheConfig;
import com.alicp.jetcache.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class ClassUtil {

    private static IdentityHashMap<Method, String> subAreaMap = new IdentityHashMap<Method, String>();
    private static IdentityHashMap<Method, String> methodSigMap = new IdentityHashMap<Method, String>();

    public static String getSubArea(CacheConfig cacheConfig, Method method, String[] hidePackages) {
        // TODO invalid cache when param type changed

        String prefix = subAreaMap.get(method);

        if (prefix == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(cacheConfig.getVersion()).append('_');
            sb.append(method.getDeclaringClass().getName());
            sb.append('.');
            getMethodSig(sb, method);
            String str = replace(hidePackages, sb);
            subAreaMap.put(method, str);
            return str;
        } else {
            return prefix;
        }
    }

    private static String replace(String[] hidePackages, StringBuilder sb) {
        String str = sb.toString();
        if (hidePackages != null) {
            for (String p : hidePackages) {
                String pWithDot = p + ".";
                str = str.replace(pWithDot, "");
                str = str.replace(pWithDot.replace('.', '/'), "");
            }
        }
        return str;
    }

    public static Class<?>[] getAllInterfaces(Object obj) {
        Class<?> c = obj.getClass();
        HashSet<Class<?>> s = new HashSet<Class<?>>();
        do {
            Class<?>[] its = c.getInterfaces();
            for (Class<?> it : its) {
                s.add(it);
            }
            c = c.getSuperclass();
        } while (c != null);
        return s.toArray(new Class<?>[s.size()]);
    }

    private static void getMethodSig(StringBuilder sb, Method m) {
        sb.append(m.getName());
        sb.append(Type.getType(m).getDescriptor());
    }

    public static String getMethodSig(Method m, String[] hidePackages) {
        String sig = methodSigMap.get(m);
        if (sig != null) {
            return sig;
        } else {
            StringBuilder sb = new StringBuilder();
            getMethodSig(sb, m);
            sig = replace(hidePackages, sb);
            methodSigMap.put(m, sig);
            return sig;
        }
    }
}
