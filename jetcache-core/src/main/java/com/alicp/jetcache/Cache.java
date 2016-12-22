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

    Logger CACHE_INTERNAL_LOGGER = LoggerFactory.getLogger(Cache.class);


    //-----------------------------JSR 107 API------------------------------------------------

    default V get(K key) {
        try {
            CacheGetResult<V> result = GET(key);
            if (result.isSuccess()) {
                return result.getValue();
            } else {
                return null;
            }
        } catch (ClassCastException ex) {
            CACHE_INTERNAL_LOGGER.warn("jetcache get error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return null;
        }
    }

    default void put(K key, V value) {
        PUT(key, value);
    }

    default boolean remove(K key) {
        return REMOVE(key).isSuccess();
    }

    /**
     * Provides a standard way to access the underlying concrete cache entry
     * implementation in order to provide access to further, proprietary features.
     * <p>
     * If the provider's implementation does not support the specified class,
     * the {@link IllegalArgumentException} is thrown.
     *
     * @param clazz the proprietary class or interface of the underlying
     *              concrete cache. It is this type that is returned.
     * @return an instance of the underlying concrete cache
     * @throws IllegalArgumentException if the caching provider doesn't support
     *                                  the specified class.
     */
    <T> T unwrap(Class<T> clazz);

    //--------------------------JetCache API---------------------------------------------

    CacheConfig config();

    /**
     * examples:
     * <pre><code>
     *   try(AutoReleaseLock lock = cache.tryLock("MyKey",100, TimeUnit.SECONDS)){
     *      if(lock != null){
     *          // do something
     *      }
     *   }
     * </code></pre>
     * @param key lockKey
     * @param expire lock expire time
     * @param timeUnit lock expire time unit
     * @return an java.lang.AutoCloseable instance, or null if lock fail
     */
    AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit);

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
                Object loadedValue = loader.apply(key);
                V castedValue = (V) loadedValue;
                if (loadedValue != null || cacheNullWhenLoaderReturnNull) {
                    put(key, castedValue);
                }
                return castedValue;
            }
        } catch (ClassCastException ex) {
            CACHE_INTERNAL_LOGGER.warn("jetcache computeIfAbsent error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return null;
        }
    }

    default V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit) {
        try {
            CacheGetResult<V> r = GET(key);
            if (r.isSuccess()) {
                return r.getValue();
            } else {
                Object loadedValue = loader.apply(key);
                V castedValue = (V) loadedValue;
                if (loadedValue != null || cacheNullWhenLoaderReturnNull) {
                    PUT(key, castedValue, expire, timeUnit);
                }
                return castedValue;
            }
        } catch (ClassCastException ex) {
            CACHE_INTERNAL_LOGGER.warn("jetcache computeIfAbsent error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return null;
        }
    }

    default void put(K key, V value, long expire, TimeUnit timeUnit) {
        PUT(key, value, expire, timeUnit);
    }

    default CacheResult PUT(K key, V value){
        return PUT(key, value, config().getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
    }

    CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit);

    CacheResult REMOVE(K key);


}
