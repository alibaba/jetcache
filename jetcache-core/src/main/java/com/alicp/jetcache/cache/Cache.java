package com.alicp.jetcache.cache;

import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Cache<K, V> {

    default V get(K key){
        CacheResult<V> result = GET(key);
        if (result.isSuccess()) {
            return result.getValue();
        } else {
            return null;
        }
    }

    CacheResult<V> GET(K key);

    /**
     * Compute value use a loader when miss.
     */
    default V computeIfAbsent(K key, Function<K, V> loader) {
        CacheResult<V> r = GET(key);
        if (r.isSuccess()) {
            return r.getValue();
        } else {
            return loader.apply(key);
        }
    }

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
