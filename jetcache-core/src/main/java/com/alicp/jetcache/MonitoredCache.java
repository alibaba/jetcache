package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MonitoredCache<K, V> implements Cache<K, V>, WrapValueCache<K, V> {

    private CacheMonitor monitor;

    private Cache<K, V> cache;
    private WrapValueCache wrapValueCache;

    public MonitoredCache(Cache<K, V> cache, CacheMonitor monitor) {
        this.cache = cache;
        this.monitor = monitor;
        if (cache instanceof WrapValueCache) {
            wrapValueCache = (WrapValueCache) cache;
        }
    }

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
    public CacheGetResult<CacheValueHolder<V>> GET_HOLDER(K key) {
        if (wrapValueCache == null) {
            throw new UnsupportedOperationException();
        }
        long t = System.currentTimeMillis();
        CacheGetResult<CacheValueHolder<V>> result = wrapValueCache.GET_HOLDER(key);
        t = System.currentTimeMillis() - t;
        monitor.afterGET(t, key, result);
        return result;
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        long t = System.currentTimeMillis();
        CacheGetResult<V> result = cache.GET(key);
        t = System.currentTimeMillis() - t;
        monitor.afterGET(t, key, result);
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
                monitor.afterLoad(t, key, v, success);
            }
            return v;
        };
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        Function<K, V> newLoader = createProxyLoader(key, loader);
        return WrapValueCache.super.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull);
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit) {
        Function<K, V> newLoader = createProxyLoader(key, loader);
        return WrapValueCache.super.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull, expire, timeUnit);
    }

    @Override
    public CacheResult PUT(K key, V value) {
        //override to prevent NullPointerException when config() is null
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT(key, value);
        t = System.currentTimeMillis() - t;
        monitor.afterPUT(t, key, value, result);
        return result;
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT(key, value, expire, timeUnit);
        t = System.currentTimeMillis() - t;
        monitor.afterPUT(t, key, value, result);
        return result;
    }

    @Override
    public CacheResult INVALIDATE(K key) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.INVALIDATE(key);
        t = System.currentTimeMillis() - t;
        monitor.afterINVALIDATE(t, key, result);
        return result;
    }
}
