package com.alicp.jetcache;

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
        monitor.afterGET(t, key, result);
        return result;
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        //TODO
        long t = System.currentTimeMillis();
        Map<K, V> r = cache.getAll(keys);
        t = System.currentTimeMillis() - t;
        CacheGetResult cacheGetResult = new CacheGetResult(CacheResultCode.SUCCESS, null, null);
        boolean first = true;
        for (K key : keys) {
            //set value?
            if (first) {
                monitor.afterGET(t, key, cacheGetResult);
            } else {
                monitor.afterGET(0, key, cacheGetResult);
            }
            first = false;
        }
        return r;
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
    public void putAll(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        cache.putAll(map, expire, timeUnit);
        t = System.currentTimeMillis() - t;
        boolean first = true;
        for (Map.Entry en : map.entrySet()) {
            if (first) {
                monitor.afterPUT(t, en.getKey(), en.getValue(), CacheResult.SUCCESS_WITHOUT_MSG);
            } else {
                monitor.afterPUT(0, en.getKey(), en.getValue(), CacheResult.SUCCESS_WITHOUT_MSG);
            }
            first = false;
        }
    }

    @Override
    public CacheResult REMOVE(K key) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.REMOVE(key);
        t = System.currentTimeMillis() - t;
        monitor.afterREMOVE(t, key, result);
        return result;
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        cache.removeAll(keys);
        t = System.currentTimeMillis() - t;
        boolean first = true;
        for (K key : keys) {
            //set value?
            if (first) {
                monitor.afterINVALIDATE(t, key, CacheResult.SUCCESS_WITHOUT_MSG);
            } else {
                monitor.afterINVALIDATE(0, key, CacheResult.SUCCESS_WITHOUT_MSG);
            }
            first = false;
        }
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
        monitor.afterPUT(t, key, value, result);
        return result;
    }
}
