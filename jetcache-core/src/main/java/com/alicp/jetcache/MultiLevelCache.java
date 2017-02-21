package com.alicp.jetcache;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCache<K, V> implements Cache<K, V> {

    private Cache[] caches;

    @SuppressWarnings("unchecked")
    public MultiLevelCache(Cache... caches) {
        this.caches = caches;
    }

    public Cache[] caches() {
        return caches;
    }

    @Override
    public CacheConfig config() {
        return null;
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        for (int i = 0; i < caches.length; i++) {
            Cache cache = caches[i];
            CacheGetResult<CacheValueHolder<V>> r1 = cache.GET(key);
            if (r1.isSuccess() && r1.getValue() != null) {
                CacheValueHolder<V> h = r1.getValue();
                long currentExpire = h.getExpireTime();
                long now = System.currentTimeMillis();
                if (now <= currentExpire) {
                    long restTtl = currentExpire - now;
                    if (restTtl > 0) {
                        PUT_caches(false, i, key, h.getValue(), restTtl, TimeUnit.MILLISECONDS);
                    }
                    return new CacheGetResult(CacheResultCode.SUCCESS, null, h.getValue());
                }
            }
        }
        return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        Set<K>[] fillArray = new Set[caches.length];
        for (int i = 0; i < caches.length; i++) {

        }
    }

    @Override
    public CacheResult PUT(K key, V value) {
        //override to prevent NullPointerException when config() is null
        return PUT_caches(true, caches.length, key, value, Integer.MIN_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        return PUT_caches(false, caches.length, key, value, expire, timeUnit);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        //override to prevent NullPointerException when config() is null
        for (Cache c : caches) {
            c.putAll(map);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        for (Cache c : caches) {
            c.putAll(map, expire, timeUnit);
        }
    }

    private CacheResult PUT_caches(boolean useDefaultExpire, int lastIndex, K key, V value, long expire, TimeUnit timeUnit) {
        boolean fail = false;
        for (int i = 0; i < lastIndex; i++) {
            Cache cache = caches[i];
            if (useDefaultExpire) {
                expire = cache.config().getDefaultExpireInMillis();
                timeUnit = TimeUnit.MILLISECONDS;
            }
            CacheValueHolder<V> h = new CacheValueHolder<>(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
            CacheResult r = cache.PUT(key, h, expire, timeUnit);
            if (!r.isSuccess()) {
                fail = true;
            }
        }
        return fail ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public CacheResult REMOVE(K key) {
        boolean fail = false;
        for (Cache cache : caches) {
            CacheResult r = cache.REMOVE(key);
            if (!r.isSuccess()) {
                fail = true;
            }
        }
        return fail ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        for (Cache cache : caches) {
            cache.removeAll(keys);
        }
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("unwrap is not supported by MultiLevelCache");
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return caches[caches.length - 1].tryLock(key, expire, timeUnit);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("putIfAbsent is not supported by MultiLevelCache");
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expire, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("PUT_IF_ABSENT is not supported by MultiLevelCache");
    }
}
