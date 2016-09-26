package com.alicp.jetcache.cache;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CompoundCache<K, V> implements Cache<K, V> {

    private CacheConfig config;
    private Cache<K, CacheValueHolder<V>> l1Cache;
    private Cache<K, CacheValueHolder<V>> l2Cache;

    @SuppressWarnings("unchecked")
    public CompoundCache(CacheConfig config, Cache l1Cache, Cache l2Cache) {
        this.config = config;
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
    }

    @Override
    public String getSubArea() {
        return config.getSubArea();
    }

    @Override
    public CacheResult<V> GET(K key) {
        CacheResult<CacheValueHolder<V>> r1 = l1Cache.GET(key);
        if (r1.isSuccess() && r1.getValue() != null) {
            CacheValueHolder<V> h = r1.getValue();
            if (h.getExpireTime() < System.currentTimeMillis()) {
                return new CacheResult<V>(CacheResultCode.EXPIRED, null);
            } else {
                V value = r1.getValue().getValue();
                return new CacheResult<V>(r1.getResultCode(), value);
            }
        } else {
            CacheResult<CacheValueHolder<V>> r2 = l2Cache.GET(key);
            if(r2.isSuccess()){
                CacheValueHolder<V> h = r2.getValue();
                l1Cache.PUT(key, r2.getValue(), config.getDefaultTtlInSeconds());
                return new CacheResult<V>(r2.getResultCode(), h.getValue());
            } else {
                CacheResultCode code = r1.getResultCode() == CacheResultCode.EXPIRED ? CacheResultCode.EXPIRED : r2.getResultCode();
                return new CacheResult<V>(code, null);
            }
        }
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
        CacheResultCode c1 = l1Cache.PUT(key, h, ttlInSeconds);
        CacheResultCode c2 = l2Cache.PUT(key, h, ttlInSeconds);
        return c1 == CacheResultCode.SUCCESS && c2 == CacheResultCode.SUCCESS ? CacheResultCode.SUCCESS : CacheResultCode.FAIL;
    }

    @Override
    public CacheResultCode INVALIDATE(K key) {
        CacheResultCode c1 = l1Cache.INVALIDATE(key);
        CacheResultCode c2 = l2Cache.INVALIDATE(key);
        return c1 == CacheResultCode.SUCCESS && c2 == CacheResultCode.SUCCESS ? CacheResultCode.SUCCESS : CacheResultCode.FAIL;
    }
}
