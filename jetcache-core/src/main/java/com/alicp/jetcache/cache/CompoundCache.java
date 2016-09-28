package com.alicp.jetcache.cache;

import java.util.concurrent.TimeUnit;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CompoundCache<K, V> implements Cache<K, V> {

    private Cache[] caches;

    @SuppressWarnings("unchecked")
    public CompoundCache(Cache... caches) {
        this.caches = caches;
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        for (int i = 0; i < caches.length; i++) {
            Cache cache = caches[i];
            CacheGetResult r1 = cache.GET(key);
            if (r1.isSuccess() && r1.getValue() != null) {
                Object cacheValue = r1.getValue();
                if (cacheValue instanceof CacheValueHolder) {
                    CacheValueHolder h = (CacheValueHolder) cacheValue;
                    long now = System.currentTimeMillis();
                    if (now > h.getExpireTime()) {
                        continue;
                    } else {
                        V value = (V) h.getValue();
                        long ttl = h.getExpireTime() - now;
                        update(i, key, h, ttl, TimeUnit.MILLISECONDS);
                        return new CacheGetResult<V>(CacheResultCode.SUCCESS, null, value);
                    }
                } else {
                    continue;
                }
            }
        }
        return CacheGetResult.NOT_EXISTS;
    }

    @Override
    public void put(K key, V value) {
        for (Cache cache : caches) {
            cache.put(key, value);
        }
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        CacheValueHolder<V> h = new CacheValueHolder<V>(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
        return update(caches.length, key, h, expire, timeUnit);
    }

    private CacheResult update(int lastIndex, K key, CacheValueHolder<V> h, long expire, TimeUnit timeUnit) {
        boolean fail = false;
        for (int i = 0; i < lastIndex; i++) {
            Cache cache = caches[i];
            CacheResult r = cache.PUT(key, h, expire, timeUnit);
            if (!r.isSuccess()) {
                fail = true;
            }
        }
        return fail ? CacheResult.FAIL : CacheResult.SUCCESS;
    }

    @Override
    public CacheResult INVALIDATE(K key) {
        boolean fail = false;
        for (Cache cache : caches) {
            CacheResult r = cache.INVALIDATE(key);
            if (!r.isSuccess()) {
                fail = true;
            }
        }
        return fail ? CacheResult.FAIL : CacheResult.SUCCESS;
    }
}
