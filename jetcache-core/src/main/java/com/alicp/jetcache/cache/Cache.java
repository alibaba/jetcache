package com.alicp.jetcache.cache;

import com.alicp.jetcache.support.CacheResult;
import com.alicp.jetcache.support.CacheResultCode;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Cache<K, V> {

    String getSubArea();

    default V get(K key){
        CacheResult<V> result = GET(key);
        if (result.isSuccess()) {
            return result.getValue();
        } else {
            return null;
        }
    }

    CacheResult<V> GET(K key);

    void put(K key, V value);

    default void put(K key, V value, int ttlInSeconds){
        PUT(key, value, ttlInSeconds);
    }

    CacheResultCode PUT(K key, V value, int ttlInSeconds);

    default void invalidate(K key){
        INVALIDATE(key);
    }

    CacheResultCode INVALIDATE(K key);

}
