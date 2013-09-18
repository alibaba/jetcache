/**
 * Created on  13-09-10 15:45
 */
package com.taobao.geek.jetcache.impl;

import com.alibaba.fastjson.JSON;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.KeyGenerator;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
class DefaultKeyGenerator implements KeyGenerator {

    @Override
    public String getKey(CacheConfig cacheConfig, Method method, Object[] args) {
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

