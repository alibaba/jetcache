package com.taobao.geek.cache;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public interface KeyGenerator {

    public String getKey(String keyPrefix, Method method, Object[] args);

}
