package com.alicp.jetcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Cache<K, V> {

    Logger logger = LoggerFactory.getLogger(Cache.class);

    CacheConfig config();

    default V get(K key) {
        try {
            CacheGetResult<V> result = GET(key);
            if (result.isSuccess()) {
                return result.getValue();
            } else {
                return null;
            }
        } catch (ClassCastException ex) {
            logger.warn("jetcache get error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return null;
        }
    }

    CacheGetResult<V> GET(K key);

    default V computeIfAbsent(K key, Function<K, V> loader) {
        return computeIfAbsent(key, loader, false);
    }

    default V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        try {
            CacheGetResult<V> r = GET(key);
            if (r.isSuccess()) {
                return r.getValue();
            } else {
                V loadedValue = loader.apply(key);
                if (loadedValue != null || cacheNullWhenLoaderReturnNull) {
                    put(key, loadedValue);
                }
                return loadedValue;
            }
        } catch (ClassCastException ex) {
            logger.warn("jetcache computeIfAbsent error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return null;
        }
    }

    default void put(K key, V value) {
        PUT(key, value, config().getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
    }

    default void put(K key, V value, long expire, TimeUnit timeUnit) {
        PUT(key, value, expire, timeUnit);
    }

    CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit);

    default void invalidate(K key) {
        INVALIDATE(key);
    }

    CacheResult INVALIDATE(K key);

}
