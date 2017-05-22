package com.alicp.jetcache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2017/5/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SimpleProxyCache<K, V> implements ProxyCache<K, V> {

    private Cache<K, V> cache;

    public SimpleProxyCache(Cache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public Cache<K, V> getTargetCache() {
        return cache;
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return cache.tryLock(key, expire, timeUnit);
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
    public CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        return cache.PUT(key, value, expireAfterWrite, timeUnit);
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
}
