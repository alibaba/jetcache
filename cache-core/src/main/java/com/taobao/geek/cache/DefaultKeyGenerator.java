/**
 * Created on  13-09-10 15:45
 */
package com.taobao.geek.cache;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public class DefaultKeyGenerator implements KeyGenerator {

    @Override
    public String getKey(String keyPrefix, Method method, Object[] args) {
        StringBuilder sb = new StringBuilder();
        if (CacheConsts.DEFAULT_KEY_PREFIX.equals(keyPrefix)) {
            sb.append(keyPrefix);
        } else {

        }
        return sb.toString();
    }
}
