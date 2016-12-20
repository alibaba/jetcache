package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCache<K, V> extends AbstractCache<K, V> {

    private Cache[] caches;
    private boolean[] canGetHolder;

    @SuppressWarnings("unchecked")
    public MultiLevelCache(Cache... caches) {
        this.caches = caches;
        canGetHolder = new boolean[caches.length];
        for (int i = 0; i < caches.length; i++) {
            canGetHolder[i] = isWrap(caches[i]);
        }
    }

    public Cache[] caches() {
        return caches;
    }

    @Override
    public CacheConfig config() {
        return null;
    }

    private boolean isWrap(Cache c) {
        if (c instanceof ProxyCache) {
            return isWrap(((ProxyCache) c).getTargetCache());
        }
        if (c instanceof WrapValueCache) {
            return true;
        }
        return false;
    }

    @Override
    public CacheGetResult<CacheValueHolder<V>> __GET_HOLDER(K key) {
        for (int i = 0; i < caches.length; i++) {
            Cache cache = caches[i];
            CacheGetResult<CacheValueHolder<V>> r1;
            if (canGetHolder[i]) {
                r1 = ((WrapValueCache) cache).__GET_HOLDER(key);
            } else {
                r1 = cache.GET(key);
            }
            if (r1.isSuccess() && r1.getValue() != null) {
                Object cacheValue = r1.getValue();
                CacheValueHolder<V> h = (CacheValueHolder<V>) cacheValue;
                long now = System.currentTimeMillis();
                if (now <= h.getExpireTime()) {
                    long restTtl = h.getExpireTime() - now; // !!!!!!!!!!!!!
                    if (restTtl > 0) {
                        PUT_caches(false, i, key, h.getValue(), restTtl, TimeUnit.MILLISECONDS);
                    }
                    return new CacheGetResult(CacheResultCode.SUCCESS, null, h);
                }
            }
        }
        return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
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

    private CacheResult PUT_caches(boolean useDefaultExpire, int lastIndex, K key, V value, long expire, TimeUnit timeUnit) {
        boolean fail = false;
        for (int i = 0; i < lastIndex; i++) {
            CacheResult r1;
            Cache cache = caches[i];
            if (useDefaultExpire) {
                expire = cache.config().getDefaultExpireInMillis();
                timeUnit = TimeUnit.MILLISECONDS;
            }
            if (canGetHolder[i]) {
                r1 = cache.PUT(key, value, expire, timeUnit);
            } else {
                CacheValueHolder<V> h = new CacheValueHolder<>(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
                r1 = cache.PUT(key, h, expire, timeUnit);
            }
            CacheResult r = r1;
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
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("unwrap is not supported by MultiLevelCache");
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return caches[caches.length - 1].tryLock(key, expire, timeUnit);
    }
}
