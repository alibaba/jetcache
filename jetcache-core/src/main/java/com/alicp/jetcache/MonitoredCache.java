package com.alicp.jetcache;

import com.alicp.jetcache.event.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MonitoredCache<K, V> implements ProxyCache<K, V> {

    private CacheMonitor monitor;

    private Cache<K, V> cache;

    public MonitoredCache(Cache<K, V> cache, CacheMonitor monitor) {
        this.cache = cache;
        this.monitor = monitor;
    }

    @Override
    public Cache<K, V> getTargetCache() {
        return cache;
    }

    public CacheMonitor getMonitor() {
        return monitor;
    }

    @Override
    public CacheConfig config() {
        return cache.config();
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        long t = System.currentTimeMillis();
        CacheGetResult<V> result = cache.GET(key);
        t = System.currentTimeMillis() - t;
        CacheGetEvent event = new CacheGetEvent(cache, t, key, result);
        monitor.afterOperation(event);
        return result;
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        MultiGetResult<K, V> result = cache.GET_ALL(keys);
        t = System.currentTimeMillis() - t;
        CacheGetAllEvent event = new CacheGetAllEvent(cache, t, keys, result);
        monitor.afterOperation(event);
        return result;
    }

    private Function<K, V> createProxyLoader(K key, Function<K, V> loader) {
        return (k) -> {
            long t = System.currentTimeMillis();
            V v = null;
            boolean success = false;
            try {
                v = loader.apply(k);
                success = true;
            } finally {
                t = System.currentTimeMillis() - t;
                CacheLoadEvent event = new CacheLoadEvent(cache, t, key, v, success);
                monitor.afterOperation(event);
            }
            return v;
        };
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        Function<K, V> newLoader = createProxyLoader(key, loader);
        return ProxyCache.super.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit) {
        Function<K, V> newLoader = createProxyLoader(key, loader);
        return ProxyCache.super.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull, expire, timeUnit);
    }

    @Override
    public CacheResult PUT(K key, V value) {
        //override to prevent NullPointerException when config() is null
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT(key, value);
        t = System.currentTimeMillis() - t;
        CachePutEvent event = new CachePutEvent(cache, t, key, value, result);
        monitor.afterOperation(event);
        return result;
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT(key, value, expire, timeUnit);
        t = System.currentTimeMillis() - t;
        CachePutEvent event = new CachePutEvent(cache, t, key, value, result);
        monitor.afterOperation(event);
        return result;
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        //override to prevent NullPointerException when config() is null
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT_ALL(map);
        t = System.currentTimeMillis() - t;
        CachePutAllEvent event = new CachePutAllEvent(cache, t, map, result);
        monitor.afterOperation(event);
        return result;
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT_ALL(map, expire, timeUnit);
        t = System.currentTimeMillis() - t;
        CachePutAllEvent event = new CachePutAllEvent(cache, t, map, result);
        monitor.afterOperation(event);
        return result;
    }

    @Override
    public CacheResult REMOVE(K key) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.REMOVE(key);
        t = System.currentTimeMillis() - t;
        CacheRemoveEvent event = new CacheRemoveEvent(cache, t, key, result);
        monitor.afterOperation(event);
        return result;
    }

    @Override
    public CacheResult REMOVE_ALL(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.REMOVE_ALL(keys);
        t = System.currentTimeMillis() - t;
        CacheRemoveAllEvent event = new CacheRemoveAllEvent(cache, t, keys, result);
        monitor.afterOperation(event);
        return result;
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return cache.tryLock(key, expire, timeUnit);
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expire, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT_IF_ABSENT(key, value, expire, timeUnit);
        t = System.currentTimeMillis() - t;
        CachePutEvent event = new CachePutEvent(cache, t, key, value, result);
        monitor.afterOperation(event);
        return result;
    }
}
