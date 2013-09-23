/**
 * Created on  13-09-13 10:54
 */
package com.taobao.geek.jetcache.impl;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.objectweb.asm.Type;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
class SubAreaUtil {
    private static IdentityHashMap<Method, String> methodMap = new IdentityHashMap<Method, String>();

    public static String getSubArea(CacheConfig cacheConfig, Method method){
        // TODO 对参数的类型发生变化做出感知

        String prefix = methodMap.get(method);

        if (prefix == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(cacheConfig.getVersion()).append('_');
            sb.append(Type.getType(method.getDeclaringClass()).getDescriptor());
            sb.append('.');
            Type t = Type.getType(method);
            sb.append(method.getName());
            sb.append(t.getDescriptor());
            methodMap.put(method, sb.toString());
            return sb.toString();
        } else {
            return prefix;
        }
    }
}
