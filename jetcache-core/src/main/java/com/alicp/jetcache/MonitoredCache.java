package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MonitoredCache<K, V> implements Cache<K, V> {

    private Cache<K, V> cache;
    private CacheMonitor monitor;

    public MonitoredCache(Cache<K, V> cache, CacheMonitor monitor) {
        this.cache = cache;
        this.monitor = monitor;
    }

    public Cache<K, V> getTargetCache() {
        return cache;
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
        monitor.afterGET(t, key, result);
        return result;
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit) {
        Function<K, V> newLoader = (k) -> {
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
        return cache.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull, expire, timeUnit);
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
