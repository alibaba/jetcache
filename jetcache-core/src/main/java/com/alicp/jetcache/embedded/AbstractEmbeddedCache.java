/**
 * Created on  13-10-17 23:01
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractEmbeddedCache<K, V> extends AbstractCache<K, V> {
    protected EmbeddedCacheConfig config;
    private IntenalMap intenalMap;

    protected abstract IntenalMap createAreaCache();

    public AbstractEmbeddedCache(EmbeddedCacheConfig config) {
        this.config = config;
        intenalMap = createAreaCache();
    }

    @Override
    public CacheConfig config() {
        return config;
    }

    private Object buildKey(K key) {
        Object newKey = key;
        Function<Object, Object> keyConvertor = config.getKeyConvertor();
        if (keyConvertor != null) {
            newKey = keyConvertor.apply(key);
        }
        return newKey;
    }

    @Override
    public CacheGetResult<CacheValueHolder<V>> GET_HOLDER(K key) {
        Object newKey = buildKey(key);
        CacheValueHolder<V> holder;
        if (config.isWeakValues()) {
            WeakReference<CacheValueHolder<V>> ref = (WeakReference<CacheValueHolder<V>>) intenalMap.getValue(newKey);
            if (ref == null) {
                return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
            } else {
                holder = ref.get();
                if (holder == null) {
                    return new CacheGetResult(CacheResultCode.NOT_EXISTS, null, "weak ref released");
                } else {
                    return getImpl(newKey, holder);
                }
            }
        } else if (config.isSoftValues()) {
            SoftReference<CacheValueHolder<V>> ref = (SoftReference<CacheValueHolder<V>>) intenalMap.getValue(newKey);
            if (ref == null) {
                return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
            } else {
                holder = ref.get();
                if (holder == null) {
                    return new CacheGetResult(CacheResultCode.NOT_EXISTS, null, "soft ref released");
                } else {
                    return getImpl(newKey, holder);
                }
            }
        } else {
            CacheValueHolder<V> cacheObject = (CacheValueHolder<V>) intenalMap.getValue(newKey);
            if (cacheObject == null) {
                return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
            } else {
                return getImpl(newKey, cacheObject);
            }
        }
    }

    private CacheGetResult<CacheValueHolder<V>> getImpl(Object newKey, CacheValueHolder<V> cacheObject) {
        if (System.currentTimeMillis() - cacheObject.getExpireTime() >= 0) {
            intenalMap.removeValue(newKey);
            return CacheGetResult.EXPIRED_WITHOUT_MSG;
        } else {
            if (config.isExpireAfterAccess()) {
                long ttlInMillis = cacheObject.getInitTtlInMillis();
                cacheObject.setExpireTime(System.currentTimeMillis() + ttlInMillis);
            }
            return new CacheGetResult(CacheResultCode.SUCCESS, null, cacheObject);
        }
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        CacheValueHolder<V> cacheObject = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
        if (config.isWeakValues()) {
            WeakReference<CacheValueHolder<V>> ref = new WeakReference(cacheObject);
            intenalMap.putValue(buildKey(key), ref);
        } else if (config.isSoftValues()) {
            SoftReference<CacheValueHolder<V>> ref = new SoftReference(cacheObject);
            intenalMap.putValue(buildKey(key), ref);
        } else {
            intenalMap.putValue(buildKey(key), cacheObject);
        }
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public CacheResult INVALIDATE(K key) {
        intenalMap.removeValue(buildKey(key));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }
}
