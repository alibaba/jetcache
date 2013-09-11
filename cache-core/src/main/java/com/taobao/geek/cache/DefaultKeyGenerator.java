/**
 * Created on  13-09-10 15:45
 */
package com.taobao.geek.cache;

import com.alibaba.fastjson.JSON;
import com.taobao.geek.cache.objectweb.asm.Type;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yeli.hl
 */
public class DefaultKeyGenerator implements KeyGenerator {

    private ConcurrentHashMap<Method, String> methodMap = new ConcurrentHashMap<Method, String>();

    @Override
    public String getKey(Method method, Object[] args, int version) {
        // TODO 对参数的类型发生变化做出感知
        StringBuilder sb = new StringBuilder();
        sb.append(version).append('_');

        String prefix = methodMap.get(method);

        if (prefix == null) {
            sb.append(Type.getType(method.getClass()).getInternalName());
            sb.append('.');
            sb.append(Type.getType(method).getInternalName());
            methodMap.put(method, sb.toString());
        } else {
            sb.append(prefix);
        }

        for (Object arg : args) {
            sb.append(',');
            sb.append(JSON.toJSONString(arg));
        }

        return sb.toString();
    }

}

