package com.alicp.jetcache.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Cache<K, V> {

    default V get(K key){
        CacheGetResult<V> result = GET(key);
        if (result.isSuccess()) {
            return result.getValue();
        } else {
            return null;
        }
    }

    CacheGetResult<V> GET(K key);

    /**
     * Compute value use a loader when miss.
     */
    default V computeIfAbsent(K key, Function<K, V> loader) {
        CacheGetResult<V> r = GET(key);
        if (r.isSuccess()) {
            return r.getValue();
        } else {
            return loader.apply(key);
        }
    }

    void put(K key, V value);

    default void put(K key, V value, long expire, TimeUnit timeUnit){
        PUT(key, value, expire, timeUnit);
    }

    CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit);

    default void invalidate(K key){
        INVALIDATE(key);
    }

    CacheResult INVALIDATE(K key);

}
