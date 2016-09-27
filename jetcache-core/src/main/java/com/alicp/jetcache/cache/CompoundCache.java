package com.alicp.jetcache.cache;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CompoundCache<K, V> implements Cache<K, V> {

    private CacheConfig config;
    private Cache[] caches;

    @SuppressWarnings("unchecked")
    public CompoundCache(CacheConfig config, Cache... caches) {
        this.config = config;
        this.caches = caches;
    }

    @Override
    public String getSubArea() {
        return config.getSubArea();
    }

    @Override
    public CacheResult<V> GET(K key) {
        for (int i = 0; i < caches.length; i++) {
            Cache cache = caches[i];
            CacheResult r1 = cache.GET(key);
            if (r1.isSuccess() && r1.getValue() != null) {
                Object cacheValue = r1.getValue();
                if (cacheValue instanceof CacheValueHolder) {
                    CacheValueHolder h = (CacheValueHolder) cacheValue;
                    long now = System.currentTimeMillis();
                    if (now > h.getExpireTime()) {
                        continue;
                    } else {
                        V value = (V) h.getValue();
                        int ttl = (int)Math.ceil((h.getExpireTime() - now) / 1000.0);
                        update(i, key, h, ttl);
                        return new CacheResult<V>(CacheResultCode.SUCCESS, value);
                    }
                } else {
                    continue;
                }

            }
        }
        return new CacheResult<V>(CacheResultCode.NOT_EXISTS, null);
    }

    @Override
    public void put(K key, V value) {
        PUT(key, value, config.getDefaultTtlInSeconds());
    }

    @Override
    public CacheResultCode PUT(K key, V value, int ttlInSeconds) {
        CacheValueHolder<V> h = new CacheValueHolder<V>();
        h.setValue(value);
        long time = System.currentTimeMillis();
        h.setCreateTime(time);
        h.setExpireTime(time + ttlInSeconds * 1000);
        return update(caches.length, key, h, ttlInSeconds);
    }

    private CacheResultCode update(int lastIndex, K key, CacheValueHolder<V> h, int ttlInSeconds) {
        boolean fail = false;
        for (int i = 0; i < lastIndex; i++) {
            Cache cache = caches[i];
            CacheResultCode code = cache.PUT(key, h, ttlInSeconds);
            if (code != CacheResultCode.SUCCESS) {
                fail = true;
            }
        }
        return fail ? CacheResultCode.FAIL : CacheResultCode.SUCCESS;
    }

    @Override
    public CacheResultCode INVALIDATE(K key) {
        boolean fail = false;
        for (Cache cache : caches) {
            CacheResultCode code = cache.INVALIDATE(key);
            if (code != CacheResultCode.SUCCESS) {
                fail = true;
            }
        }
        return fail ? CacheResultCode.FAIL : CacheResultCode.SUCCESS;
    }
}
