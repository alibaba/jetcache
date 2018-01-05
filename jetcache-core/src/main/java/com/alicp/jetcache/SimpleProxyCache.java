package com.alicp.jetcache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2017/5/17.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SimpleProxyCache<K, V> implements ProxyCache<K, V> {

    protected Cache<K, V> cache;

    public SimpleProxyCache(Cache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public CacheConfig<K, V> config() {
        return cache.config();
    }

    @Override
    public Cache<K, V> getTargetCache() {
        return cache;
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return cache.getAll(keys);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return cache.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(K key) {
        return cache.remove(key);
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        cache.removeAll(keys);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return cache.unwrap(clazz);
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return cache.tryLock(key, expire, timeUnit);
    }

    @Override
    public boolean tryLockAndRun(K key, long expire, TimeUnit timeUnit, Runnable action) {
        return cache.tryLockAndRun(key, expire, timeUnit, action);
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        return cache.GET(key);
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        return cache.GET_ALL(keys);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader) {
        return cache.computeIfAbsent(key, loader);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        return cache.computeIfAbsent(key, loader, cacheNullWhenLoaderReturnNull);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expireAfterWrite, TimeUnit timeUnit) {
        return cache.computeIfAbsent(key, loader, cacheNullWhenLoaderReturnNull, expireAfterWrite, timeUnit);
    }

    @Override
    public void put(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        cache.put(key, value, expireAfterWrite, timeUnit);
    }

    @Override
    public CacheResult PUT(K key, V value) {
        return cache.PUT(key, value);
    }

    @Override
    public CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        return cache.PUT(key, value, expireAfterWrite, timeUnit);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        cache.putAll(map, expireAfterWrite, timeUnit);
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        return cache.PUT_ALL(map);
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        return cache.PUT_ALL(map, expireAfterWrite, timeUnit);
    }

    @Override
    public CacheResult REMOVE(K key) {
        return cache.REMOVE(key);
    }

    @Override
    public CacheResult REMOVE_ALL(Set<? extends K> keys) {
        return cache.REMOVE_ALL(keys);
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        return cache.PUT_IF_ABSENT(key, value, expireAfterWrite, timeUnit);
    }

    @Override
    public void close() {
        cache.close();
    }
}
