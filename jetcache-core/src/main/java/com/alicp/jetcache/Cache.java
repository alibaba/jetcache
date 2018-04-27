package com.alicp.jetcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * The cache interface, null value is supported.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface Cache<K, V> extends Closeable {

    Logger logger = LoggerFactory.getLogger(Cache.class);

    //-----------------------------JSR 107 style API------------------------------------------------

    /**
     * Gets an entry from the cache.
     * <p>If the cache's builder has specified a {@link CacheLoader} and there is no association in the cache
     * , it will attempt to load the entry.</p>
     * <p>If error occurs during cache access, the method return null instead of throwing an exception.</p>
     * @param key the key whose associated value is to be returned
     * @return the associated value. null may indicates: <ul>
     *     <li>the entry does not exist or expired</li>
     *     <li>the entry value is null</li>
     *     <li>error occurs during cache access(no exception throws)</li>
     * </ul>
     * @throws CacheInvokeException only if loader throws an exception
     * @see CacheLoader
     * @see #GET(Object)
     */
    default V get(K key) throws CacheInvokeException {
        CacheGetResult<V> result = GET(key);
        if (result.isSuccess()) {
            return result.getValue();
        } else {
            return null;
        }
    }

    /**
     * Gets a collection of entries from the Cache, returning them as Map of the values associated with
     * the set of keys requested.
     * <p>If the cache's builder has specified a {@link CacheLoader} and there is no association in the cache
     * , it will attempt to load the entry.</p>
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * @param keys The keys whose associated values are to be returned.
     * @return A map of entries that were found for the given keys. Keys not found in the cache are not in the returned map.
     * @throws CacheInvokeException only if loader throws an exception
     * @see CacheLoader
     * @see #GET_ALL(Set)
     */
    default Map<K, V> getAll(Set<? extends K> keys) throws CacheInvokeException {
        MultiGetResult<K, V> cacheGetResults = GET_ALL(keys);
        return cacheGetResults.unwrapValues();
    }

    /**
     * Associates the specified value with the specified key in the cache.
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * <p>if the implementation supports asynchronous operation, the cache operation of this method is asynchronous.</p>
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @see #PUT(Object, Object)
     */
    default void put(K key, V value) {
        PUT(key, value);
    }

    /**
     * Copies all of the entries from the specified map to the cache.
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * <p>if the implementation supports asynchronous operation, the cache operation of this method is asynchronous.</p>
     * @param map mappings to be stored in this cache.
     * @see #PUT_ALL(Map)
     */
    default void putAll(Map<? extends K, ? extends V> map) {
        PUT_ALL(map);
    }

    /**
     * Atomically associates the specified key with the given value if it is not already associated with a value.
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * <p>{@link MultiLevelCache} does not support this method.</p>
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return true if a value was set, false if the KV association does not exists in the cache,
     *         or error occurs during cache access.
     * @see #PUT_IF_ABSENT(Object, Object, long, TimeUnit)
     */
    default boolean putIfAbsent(K key, V value) {
        CacheResult result = PUT_IF_ABSENT(key, value, config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS);
        return result.getResultCode() == CacheResultCode.SUCCESS;
    }

    /**
     * Removes the mapping for a key from this cache if it is present.
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * @param key key whose mapping is to be removed from the cache
     * @return true if the key is removed successfully, false if the KV association does not exists in the cache,
     *         or error occurs during cache access.
     * @see #REMOVE(Object)
     */
    default boolean remove(K key) {
        return REMOVE(key).isSuccess();
    }

    /**
     * Removes entries for the specified keys.
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * <p>if the implementation supports asynchronous operation, the cache operation of this method is asynchronous.</p>
     * @param keys the keys to remove
     * @see #REMOVE_ALL(Set)
     */
    default void removeAll(Set<? extends K> keys) {
        REMOVE_ALL(keys);
    }

    /**
     * Provides a standard way to access the underlying concrete cache entry
     * implementation in order to provide access to further, proprietary features.
     * <p>
     * If the implementation does not support the specified class,
     * the {@link IllegalArgumentException} is thrown.
     *
     * @param clazz the proprietary class or interface of the underlying
     *              concrete cache. It is this type that is returned.
     * @return an instance of the underlying concrete cache
     * @throws IllegalArgumentException if the caching provider doesn't support
     *                                  the specified class.
     */
    <T> T unwrap(Class<T> clazz);

    /**
     * Clean resources created by this cache.
     */
    default void close() {
    }

    //--------------------------JetCache API---------------------------------------------

    /**
     * Get the config of this cache.
     * @return the cache config
     */
    CacheConfig<K, V> config();

    /**
     * Use this cache attempt to acquire a exclusive lock specified by the key, this method will not block.
     * examples:
     * <pre>
     *   try(AutoReleaseLock lock = cache.tryLock("MyKey",100, TimeUnit.SECONDS)){
     *      if(lock != null){
     *          // do something
     *      }
     *   }
     * </pre>
     * <p>{@link MultiLevelCache} will use the last level cache to support this operation.</p>
     * @param key      lockKey
     * @param expire   lock expire time
     * @param timeUnit lock expire time unit
     * @return an AutoReleaseLock(implements java.lang.AutoCloseable) instance if success.
     *         or null if the attempt fails, which indicates there is an another thread/process/server has the lock,
     *         or error occurs during cache access.
     * @see #tryLockAndRun(Object, long, TimeUnit, Runnable)
     */
    @SuppressWarnings("unchecked")
    default AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return null;
        }
        final String uuid = UUID.randomUUID().toString();
        final long expireTimestamp = System.currentTimeMillis() + timeUnit.toMillis(expire);
        final CacheConfig config = config();


        AutoReleaseLock lock = () -> {
            int unlockCount = 0;
            while (unlockCount++ < config.getTryLockUnlockCount()) {
                if(System.currentTimeMillis() < expireTimestamp) {
                    CacheResult unlockResult = REMOVE(key);
                    if (unlockResult.getResultCode() == CacheResultCode.FAIL
                            || unlockResult.getResultCode() == CacheResultCode.PART_SUCCESS) {
                        logger.info("[tryLock] [{} of {}] [{}] unlock failed. Key={}, msg = {}",
                                unlockCount, config.getTryLockUnlockCount(), uuid, key, unlockResult.getMessage());
                        // retry
                    } else if (unlockResult.isSuccess()) {
                        logger.debug("[tryLock] [{} of {}] [{}] successfully release the lock. Key={}",
                                unlockCount, config.getTryLockUnlockCount(), uuid, key);
                        return;
                    } else {
                        logger.warn("[tryLock] [{} of {}] [{}] unexpected unlock result: Key={}, result={}",
                                unlockCount, config.getTryLockUnlockCount(), uuid, key, unlockResult.getResultCode());
                        return;
                    }
                } else {
                    logger.info("[tryLock] [{} of {}] [{}] lock already expired: Key={}",
                            unlockCount, config.getTryLockUnlockCount(), uuid, key);
                    return;
                }
            }
        };

        int lockCount = 0;
        Cache cache = this;
        while (lockCount++ < config.getTryLockLockCount()) {
            CacheResult lockResult = cache.PUT_IF_ABSENT(key, uuid, expire, timeUnit);
            if (lockResult.isSuccess()) {
                logger.debug("[tryLock] [{} of {}] [{}] successfully get a lock. Key={}",
                        lockCount, config.getTryLockLockCount(), uuid, key);
                return lock;
            } else if (lockResult.getResultCode() == CacheResultCode.FAIL || lockResult.getResultCode() == CacheResultCode.PART_SUCCESS) {
                logger.info("[tryLock] [{} of {}] [{}] cache access failed during get lock, will inquiry {} times. Key={}, msg={}",
                        lockCount, config.getTryLockLockCount(), uuid,
                        config.getTryLockInquiryCount(), key, lockResult.getMessage());
                int inquiryCount = 0;
                while (inquiryCount++ < config.getTryLockInquiryCount()) {
                    CacheGetResult inquiryResult = cache.GET(key);
                    if (inquiryResult.isSuccess()) {
                        if (uuid.equals(inquiryResult.getValue())) {
                            logger.debug("[tryLock] [{} of {}] [{}] successfully get a lock after inquiry. Key={}",
                                    inquiryCount, config.getTryLockInquiryCount(), uuid, key);
                            return lock;
                        } else {
                            logger.debug("[tryLock] [{} of {}] [{}] not the owner of the lock, return null. Key={}",
                                    inquiryCount, config.getTryLockInquiryCount(), uuid, key);
                            return null;
                        }
                    } else {
                        logger.info("[tryLock] [{} of {}] [{}] inquiry failed. Key={}, msg={}",
                                inquiryCount, config.getTryLockInquiryCount(), uuid, key, inquiryResult.getMessage());
                        // retry inquiry
                    }
                }
            } else {
                // others holds the lock
                logger.debug("[tryLock] [{} of {}] [{}] others holds the lock, return null. Key={}",
                        lockCount, config.getTryLockLockCount(), uuid, key);
                return null;
            }
        }

        logger.debug("[tryLock] [{}] return null after {} attempts. Key={}", uuid, config.getTryLockLockCount(), key);
        return null;
    }

    /**
     * Use this cache to try run an action exclusively.
     * <p>{@link MultiLevelCache} will use the last level cache to support this operation.</p>
     * examples:
     * <pre>
     * cache.tryLock("MyKey",100, TimeUnit.SECONDS),() -&gt; {
     *     //do something
     * });
     * </pre>
     * @param key lockKey
     * @param expire lock expire time
     * @param timeUnit lock expire time unit
     * @param action the action need to execute
     * @return true if successfully get the lock and the action is executed
     */
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

    /**
     * Gets an entry from the cache.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage()/getValue() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param key the key
     * @return the result
     */
    CacheGetResult<V> GET(K key);

    /**
     * Gets a collection of entries from the Cache.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage()/getValue() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param keys the key collection
     * @return the result
     */
    MultiGetResult<K, V> GET_ALL(Set<? extends K> keys);

    /**
     * If there is a value associated with the key, return the value;
     * otherwise use the loader load the value and return, and then update the cache.
     * @param key the key
     * @param loader the value loader
     * @return the value
     * @see CacheConfig#isCacheNullValue()
     */
    default V computeIfAbsent(K key, Function<K, V> loader) {
        return computeIfAbsent(key, loader, config().isCacheNullValue());
    }

    /**
     * If there is a value associated with the key, return the value;
     * otherwise use the loader load the value and return, and then update the cache.
     * @param key the key
     * @param loader the value loader
     * @param cacheNullWhenLoaderReturnNull true if null value returned by loader should put into cache use the key
     * @return the value
     */
    V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull);

    /**
     * If there is a value associated with the key, return the value;
     * otherwise use the loader load the value and return, and then update the cache.
     * @param key the key
     * @param loader the value loader
     * @param cacheNullWhenLoaderReturnNull true if null value returned by loader should put into cache use the key
     * @param expireAfterWrite the TTL(time to live) of the KV association
     * @param timeUnit the time unit of expireAfterWrite
     * @return the value
     */
    V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * Associates the specified value with the specified key in the cache.
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param expireAfterWrite the TTL(time to live) of the KV association
     * @param timeUnit the time unit of expireAfterWrite
     * @see #PUT(Object, Object, long, TimeUnit)
     */
    default void put(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        PUT(key, value, expireAfterWrite, timeUnit);
    }

    /**
     * Associates the specified value with the specified key in the cache.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the result
     */
    default CacheResult PUT(K key, V value) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT(key, value, config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Associates the specified value with the specified key in the cache.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param expireAfterWrite the TTL(time to live) of the KV association
     * @param timeUnit the time unit of expireAfterWrite
     * @return the result
     */
    CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * Copies all of the entries from the specified map to the cache.
     * <p>If error occurs during cache access, the method will not throw an exception.</p>
     * @param map mappings to be stored in this cache.
     * @param expireAfterWrite the TTL(time to live) of the KV association
     * @param timeUnit the time unit of expireAfterWrite
     * @see #PUT_ALL(Map, long, TimeUnit)
     */
    default void putAll(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        PUT_ALL(map, expireAfterWrite, timeUnit);
    }

    /**
     * Copies all of the entries from the specified map to the cache.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param map mappings to be stored in this cache.
     * @return the result
     */
    default CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT_ALL(map, config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Copies all of the entries from the specified map to the cache.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param map mappings to be stored in this cache.
     * @param expireAfterWrite the TTL(time to live) of the KV association
     * @param timeUnit the time unit of expireAfterWrite
     * @return the result
     */
    CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * Removes the mapping for a key from this cache if it is present.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param key key whose mapping is to be removed from the cache
     * @return the result
     */
    CacheResult REMOVE(K key);

    /**
     * Removes entries for the specified keys.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param keys the keys to remove
     * @return the result
     */
    CacheResult REMOVE_ALL(Set<? extends K> keys);

    /**
     * If the specified key is not already associated with a value, associate it with the given value.
     * <p>if the implementation supports asynchronous operation, the cache access may not completed after this method
     * return. The invoke of getResultCode()/isSuccess()/getMessage() on the result will block until cache
     * operation is completed. Call future() method on the result will get a CompletionStage instance for asynchronous
     * programming.</p>
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param expireAfterWrite the TTL(time to live) of the KV association
     * @param timeUnit the time unit of expireAfterWrite
     * @return SUCCESS if the specified key is not already associated with a value,
     * or EXISTS if the specified key is not already associated with a value,
     * or FAIL if error occurs.
     */
    CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

}
