/**
 * Created on  13-10-17 23:01
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.cache.CacheValueHolder;
import com.alicp.jetcache.cache.CacheResult;
import com.alicp.jetcache.cache.CacheResultCode;

import java.lang.ref.SoftReference;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractLocalCache<K, V> implements Cache<K, V> {
    protected LocalCacheConfig config;
    private AreaCache areaCache;

    protected abstract AreaCache createAreaCache();

    public AbstractLocalCache(LocalCacheConfig config){
        this.config = config;
        areaCache = createAreaCache();
    }

    @Override
    public String getSubArea() {
        return config.getSubArea();
    }

    @Override
    public CacheResult<V> GET(K key) {
        CacheResultCode code;
        Object value = null;
        try {
            if (config.isUseSoftRef()) {
                SoftReference<CacheValueHolder<V>> ref = (SoftReference<CacheValueHolder<V>>) areaCache.getValue(key);
                if (ref == null) {
                    code = CacheResultCode.NOT_EXISTS;
                } else {
                    CacheValueHolder<V> cacheObject = ref.get();
                    if (cacheObject == null) {
                        code = CacheResultCode.NOT_EXISTS;
                    } else {
                        if (System.currentTimeMillis() - cacheObject.getExpireTime() >= 0) {
                            areaCache.removeValue(key);
                            code = CacheResultCode.EXPIRED;
                        } else {
                            code = CacheResultCode.SUCCESS;
                            value = cacheObject.getValue();
                        }
                    }
                }
            } else {
                CacheValueHolder<V> cacheObject = (CacheValueHolder<V>) areaCache.getValue(key);
                if (cacheObject == null) {
                    code = CacheResultCode.NOT_EXISTS;
                } else {
                    if (System.currentTimeMillis() - cacheObject.getExpireTime() >= 0) {
                        areaCache.removeValue(key);
                        code = CacheResultCode.EXPIRED;
                    } else {
                        code = CacheResultCode.SUCCESS;
                        value = cacheObject.getValue();
                    }
                }
            }
        } catch (Exception e) {
            code = CacheResultCode.FAIL;
        }
        return new CacheResult(code, value);
    }

    @Override
    public void put(K key, V value) {
        PUT(key, value, config.getDefaultTtlInSeconds());
    }

    @Override
    public CacheResultCode PUT(K key, V value, int expire) {
        CacheValueHolder<V> cacheObject = null;
        if (value instanceof CacheValueHolder) {
            cacheObject = (CacheValueHolder<V>) value;
        } else {
            cacheObject = new CacheValueHolder<V>();
            cacheObject.setValue(value);
            long now = System.currentTimeMillis();
            cacheObject.setExpireTime(1000 * expire + now);
            cacheObject.setCreateTime(now);
        }
        if (config.isUseSoftRef()) {
            SoftReference<CacheValueHolder<V>> ref = new SoftReference(cacheObject);
            areaCache.putValue(key, ref);
        } else {
            areaCache.putValue(key, cacheObject);
        }
        return CacheResultCode.SUCCESS;
    }

    @Override
    public CacheResultCode INVALIDATE(K key) {
        areaCache.removeValue(key);
        return CacheResultCode.SUCCESS;
    }
}
