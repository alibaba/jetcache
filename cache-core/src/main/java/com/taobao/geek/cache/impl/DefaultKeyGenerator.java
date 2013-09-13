/**
 * Created on  13-09-10 15:45
 */
package com.taobao.geek.cache.impl;

import com.alibaba.fastjson.JSON;
import com.taobao.geek.cache.CacheConfig;
import com.taobao.geek.cache.KeyGenerator;
import com.taobao.geek.cache.objectweb.asm.Type;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yeli.hl
 */
class DefaultKeyGenerator implements KeyGenerator {

    @Override
    public String getKey(CacheConfig cacheConfig, Method method, Object[] args, int version) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            sb.append(JSON.toJSONString(arg));
            if (i < args.length - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

}

