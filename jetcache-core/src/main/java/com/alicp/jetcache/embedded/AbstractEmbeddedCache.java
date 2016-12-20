/**
 * Created on  13-10-17 23:01
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractEmbeddedCache<K, V> extends AbstractCache<K, V> {
    protected EmbeddedCacheConfig config;
    protected InnerMap innerMap;

    protected abstract InnerMap createAreaCache();

    public AbstractEmbeddedCache(EmbeddedCacheConfig config) {
        this.config = config;
        innerMap = createAreaCache();
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
    public CacheGetResult<CacheValueHolder<V>> __GET_HOLDER(K key) {
        Object newKey = buildKey(key);

        CacheValueHolder<V> cacheObject = (CacheValueHolder<V>) innerMap.getValue(newKey);
        if (cacheObject == null) {
            return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
        } else {
            return getImpl(newKey, cacheObject);
        }
    }

    private CacheGetResult<CacheValueHolder<V>> getImpl(Object newKey, CacheValueHolder<V> cacheObject) {
        if (System.currentTimeMillis() > cacheObject.getExpireTime()) {
            innerMap.removeValue(newKey);
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
        innerMap.putValue(buildKey(key), cacheObject);
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public CacheResult REMOVE(K key) {
        innerMap.removeValue(buildKey(key));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }
}
