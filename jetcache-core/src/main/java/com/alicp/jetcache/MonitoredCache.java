package com.alicp.jetcache;

import com.alicp.jetcache.event.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MonitoredCache<K, V> implements ProxyCache<K, V> {

    private CacheMonitor[] monitors;

    private Cache<K, V> cache;

    public MonitoredCache(Cache<K, V> cache, CacheMonitor... monitor) {
        this.cache = cache;
        Objects.requireNonNull(monitor);
        this.monitors = monitor;
    }

    @Override
    public Cache<K, V> getTargetCache() {
        return cache;
    }

    public CacheMonitor[] getMonitors() {
        return monitors;
    }

    public void notify(CacheEvent e) {
        for (CacheMonitor m : monitors) {
            m.afterOperation(e);
        }
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        long t = System.currentTimeMillis();
        CacheGetResult<V> result = cache.GET(key);
        result.future().thenRun(() -> {
            CacheGetEvent event = new CacheGetEvent(cache, System.currentTimeMillis() - t, key, result);
            notify(event);
        });
        return result;
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        MultiGetResult<K, V> result = cache.GET_ALL(keys);
        result.future().thenRun(() -> {
            CacheGetAllEvent event = new CacheGetAllEvent(cache, System.currentTimeMillis() - t, keys, result);
            notify(event);
        });
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
                notify(event);
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
    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expireAfterWrite, TimeUnit timeUnit) {
        Function<K, V> newLoader = createProxyLoader(key, loader);
        return ProxyCache.super.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull, expireAfterWrite, timeUnit);
    }

    @Override
    public CacheResult PUT(K key, V value) {
        //override to prevent NullPointerException when config() is null
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT(key, value);
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(cache, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    @Override
    public CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT(key, value, expireAfterWrite, timeUnit);
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(cache, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        //override to prevent NullPointerException when config() is null
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT_ALL(map);
        result.future().thenRun(() -> {
            CachePutAllEvent event = new CachePutAllEvent(cache, System.currentTimeMillis() - t, map, result);
            notify(event);
        });
        return result;
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT_ALL(map, expireAfterWrite, timeUnit);
        result.future().thenRun(() -> {
            CachePutAllEvent event = new CachePutAllEvent(cache, System.currentTimeMillis() - t, map, result);
            notify(event);
        });
        return result;
    }

    @Override
    public CacheResult REMOVE(K key) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.REMOVE(key);
        result.future().thenRun(() -> {
            CacheRemoveEvent event = new CacheRemoveEvent(cache, System.currentTimeMillis() - t, key, result);
            notify(event);
        });
        return result;
    }

    @Override
    public CacheResult REMOVE_ALL(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.REMOVE_ALL(keys);
        result.future().thenRun(() -> {
            CacheRemoveAllEvent event = new CacheRemoveAllEvent(cache, System.currentTimeMillis() - t, keys, result);
            notify(event);
        });
        return result;
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return cache.tryLock(key, expire, timeUnit);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        //override to prevent NullPointerException when config() is null
        if (cache instanceof ConfigAwareCache) {
            CacheResult result = PUT_IF_ABSENT(key, value, config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS);
            return result.getResultCode() == CacheResultCode.SUCCESS;
        } else {
            long t = System.currentTimeMillis();
            boolean b = cache.putIfAbsent(key, value);
            CacheResult result = b ? CacheResult.SUCCESS_WITHOUT_MSG : CacheResult.FAIL_WITHOUT_MSG;
            CachePutEvent event = new CachePutEvent(cache, System.currentTimeMillis() - t, key, value, result);
            notify(event);
            return b;
        }
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = cache.PUT_IF_ABSENT(key, value, expireAfterWrite, timeUnit);
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(cache, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }
}
