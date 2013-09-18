/**
 * Created on  13-09-09 17:20
 */
package com.taobao.geek.jetcache.impl;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * @author yeli.hl
 */
class ClassUtil {
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

    public static String getMethodSig(Method m) {
        return m.getName() + "_" + com.taobao.geek.jetcache.objectweb.asm.Type.getType(m).getDescriptor();
    }
}
