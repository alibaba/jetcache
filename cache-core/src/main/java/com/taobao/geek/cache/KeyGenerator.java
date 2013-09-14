package com.taobao.geek.cache;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public interface KeyGenerator {

    public String getKey(CacheConfig cacheConfig, Method method, Object[] args);

}
