package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCache<K, V> extends WrapValueCache<K, V> {

    private Cache[] caches;
    private boolean[] isWrap;

    @SuppressWarnings("unchecked")
    public MultiLevelCache(Cache... caches) {
        this.caches = caches;
        isWrap = new boolean[caches.length];
        for (int i = 0; i < caches.length; i++) {
            Cache c = caches[i];
            while (c instanceof DelegateCache) {
                c = ((DelegateCache) c).getTargetCache();
            }
            isWrap[i] = c instanceof WrapValueCache;
        }
    }

    @Override
    public CacheConfig config() {
        return null;
    }

    @Override
    public CacheGetResult<CacheValueHolder<V>> GET_HOLDER(K key) {
        for (int i = 0; i < caches.length; i++) {
            Cache cache = caches[i];
            CacheGetResult<CacheValueHolder<V>> r1 = null;
            if (isWrap[i]) {
                r1 = ((WrapValueCache) cache).GET_HOLDER(key);
            } else {
                r1 = cache.GET(key);
            }
            if (r1.isSuccess() && r1.getValue() != null) {
                Object cacheValue = r1.getValue();
                CacheValueHolder<V> h = (CacheValueHolder<V>) cacheValue;
                long now = System.currentTimeMillis();
                if (now > h.getExpireTime()) {
                    continue;
                } else {
                    long restTtl = h.getExpireTime() - now; // !!!!!!!!!!!!!
                    PUT_caches(i, key, h.getValue(), restTtl, TimeUnit.MILLISECONDS);
                    return new CacheGetResult(CacheResultCode.SUCCESS, null, h);
                }
            }
        }
        return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
    }

    @Override
    public void put(K key, V value) {
        for (int i = 0; i < caches.length; i++) {
            long defaultTtl = caches[i].config().getDefaultExpireInMillis();
            PUT_impl(i, key, value, defaultTtl, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        return PUT_caches(caches.length, key, value, expire, timeUnit);
    }

    private CacheResult PUT_caches(int lastIndex, K key, V value, long expire, TimeUnit timeUnit) {
        boolean fail = false;
        for (int i = 0; i < lastIndex; i++) {
            CacheResult r = PUT_impl(i, key, value, expire, timeUnit);
            if (!r.isSuccess()) {
                fail = true;
            }
        }
        return fail ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.SUCCESS_WITHOUT_MSG;
    }

    private CacheResult PUT_impl(int index, K key, V value, long expire, TimeUnit timeUnit) {
        CacheResult r = null;
        Cache cache = caches[index];
        if (isWrap[index]) {
            r = cache.PUT(key, value, expire, timeUnit);
        } else {
            CacheValueHolder<V> h = new CacheValueHolder<V>(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
            r = cache.PUT(key, h, expire, timeUnit);
        }
        return r;
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
        return fail ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.SUCCESS_WITHOUT_MSG;
    }
}
