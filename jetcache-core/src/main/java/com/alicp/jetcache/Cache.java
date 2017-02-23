package com.alicp.jetcache;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Cache<K, V> {

    //-----------------------------JSR 107 API------------------------------------------------

    default V get(K key) {
        CacheGetResult<V> result = GET(key);
        if (result.isSuccess()) {
            return result.getValue();
        } else {
            return null;
        }
    }

    default Map<K, V> getAll(Set<? extends K> keys) {
        MultiGetResult<K, V> cacheGetResults = GET_ALL(keys);
        return cacheGetResults.unwrapValues();
    }

    default void put(K key, V value) {
        PUT(key, value);
    }

    default void putAll(Map<? extends K, ? extends V> map) {
        PUT_ALL(map);
    }

    default boolean putIfAbsent(K key, V value) {
        CacheResult result = PUT_IF_ABSENT(key, value, config().getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
        return result.getResultCode() == CacheResultCode.SUCCESS;
    }

    default boolean remove(K key) {
        return REMOVE(key).isSuccess();
    }

    default void removeAll(Set<? extends K> keys) {
        removeAll(keys);
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
     *
     * @param key      lockKey
     * @param expire   lock expire time
     * @param timeUnit lock expire time unit
     * @return an java.lang.AutoCloseable instance, or null if lock fail
     */
    AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit);

    default boolean tryLockAndRun(K key, long expire, TimeUnit timeUnit, Runnable action){
        try (AutoReleaseLock lock = tryLock(key, expire, timeUnit)) {
            if (lock != null) {
                action.run();
                return true;
            } else {
                return false;
            }
        }
    }

    CacheGetResult<V> GET(K key);

    MultiGetResult<K, V> GET_ALL(Set<? extends K> keys);

    default V computeIfAbsent(K key, Function<K, V> loader) {
        return computeIfAbsent(key, loader, false);
    }

    default V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
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
    }

    default V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit) {
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
    }

    default void put(K key, V value, long expire, TimeUnit timeUnit) {
        PUT(key, value, expire, timeUnit);
    }

    default CacheResult PUT(K key, V value) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT(key, value, config().getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
    }

    CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit);

    default void putAll(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        PUT_ALL(map, expire, timeUnit);
    }

    default CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT_ALL(map, config().getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
    }

    CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit);

    CacheResult REMOVE(K key);

    CacheResult REMOVE_ALL(Set<? extends K> keys);

    /**
     * If the specified key is not already associated
     * with a value, associate it with the given value.
     *
     * @param key
     * @param value
     * @param expire
     * @param timeUnit
     * @return SUCCESS if the specified key is not already associated with a value,
     * or EXISTS if the specified key is not already associated with a value,
     * or FAIL if error occurs.
     */
    CacheResult PUT_IF_ABSENT(K key, V value, long expire, TimeUnit timeUnit);

}
