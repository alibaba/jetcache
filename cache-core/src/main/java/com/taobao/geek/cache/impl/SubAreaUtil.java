/**
 * Created on  13-09-13 10:54
 */
package com.taobao.geek.cache.impl;

import com.taobao.geek.cache.CacheConfig;
import com.taobao.geek.cache.objectweb.asm.Type;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
class SubAreaUtil {
    private static IdentityHashMap<Method, String> methodMap = new IdentityHashMap<Method, String>();

    public static String getSubArea(CacheConfig cacheConfig, Method method){
        // TODO 对参数的类型发生变化做出感知
        StringBuilder sb = new StringBuilder();
        sb.append(cacheConfig.getVersion()).append('_');

        String prefix = methodMap.get(method);

        if (prefix == null) {
            sb.append(Type.getType(method.getClass()).getInternalName());
            sb.append('.');
            sb.append(Type.getType(method).getInternalName());
            methodMap.put(method, sb.toString());
        } else {
            sb.append(prefix);
        }
        return prefix;
    }
}
