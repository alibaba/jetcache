/**
 * Created on  13-10-17 23:01
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;

import java.lang.ref.SoftReference;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractEmbeddedCache<K, V> implements Cache<K, V> {
    protected EmbeddedCacheConfig config;
    private AreaCache areaCache;

    protected abstract AreaCache createAreaCache();

    public AbstractEmbeddedCache(EmbeddedCacheConfig config){
        this.config = config;
        areaCache = createAreaCache();
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        try {
            if (config.isUseSoftRef()) {
                SoftReference<CacheValueHolder<V>> ref = (SoftReference<CacheValueHolder<V>>) areaCache.getValue(key);
                if (ref == null) {
                    return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
                } else {
                    CacheValueHolder<V> cacheObject = ref.get();
                    if (cacheObject == null) {
                        return new CacheGetResult(CacheResultCode.NOT_EXISTS, null, "soft ref released");
                    } else {
                        return getImpl(key, cacheObject);
                    }
                }
            } else {
                CacheValueHolder<V> cacheObject = (CacheValueHolder<V>) areaCache.getValue(key);
                if (cacheObject == null) {
                    return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
                } else {
                    return getImpl(key, cacheObject);
                }
            }
        } catch (Exception e) {
            return new CacheGetResult(CacheResultCode.FAIL, null, e.getClass().getName() + ":" + e.getMessage());
        }
    }

    private CacheGetResult<V> getImpl(K key, CacheValueHolder<V> cacheObject){
        if (System.currentTimeMillis() - cacheObject.getExpireTime() >= 0) {
            areaCache.removeValue(key);
            return CacheGetResult.EXPIRED_WITHOUT_MSG;
        } else {
            if(config.isExpireAfterAccess()){
                long ttlInMillis = cacheObject.getTtlInMillis();
                cacheObject.setExpireTime(System.currentTimeMillis() + ttlInMillis);
            }
            return new CacheGetResult<V>(CacheResultCode.SUCCESS, null, cacheObject.getValue());
        }
    }

    @Override
    public void put(K key, V value) {
        PUT(key, value, config.getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        CacheValueHolder<V> cacheObject = null;
        if (value instanceof CacheValueHolder) {
            cacheObject = (CacheValueHolder<V>) value;
        } else {
            cacheObject = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
        }
        if (config.isUseSoftRef()) {
            SoftReference<CacheValueHolder<V>> ref = new SoftReference(cacheObject);
            areaCache.putValue(key, ref);
        } else {
            areaCache.putValue(key, cacheObject);
        }
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public CacheResult INVALIDATE(K key) {
        areaCache.removeValue(key);
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }
}
