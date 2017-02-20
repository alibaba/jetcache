/**
 * Created on  13-10-17 23:01
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    protected Object buildKey(K key) {
        Object newKey = key;
        Function<Object, Object> keyConvertor = config.getKeyConvertor();
        if (keyConvertor != null) {
            newKey = keyConvertor.apply(key);
        }
        return newKey;
    }

    @Override
    protected CacheGetResult<CacheValueHolder<V>> getHolder(K key) {
        Object newKey = buildKey(key);

        CacheValueHolder<V> cacheObject = (CacheValueHolder<V>) innerMap.getValue(newKey);
        if (cacheObject == null) {
            return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
        } else {
            return getImpl(newKey, cacheObject);
        }
    }

    @Override
    protected List<CacheValueHolder<V>> getHolder(List<? extends K> keys) {
        List newKeys = keys.stream().map((k)-> buildKey(k)).collect(Collectors.toList());
        List<CacheValueHolder<V>> result = innerMap.getAllValues(newKeys);
        for (int i = 0; i < result.size(); i++) {
            CacheValueHolder<V> h = result.get(i);
            Object newKey = newKeys.get(i);
            if(!getImpl(newKey, h).isSuccess()){
                result.set(i, null);
            }
        }
        return result;
    }

    private CacheGetResult<CacheValueHolder<V>> getImpl(Object newKey, CacheValueHolder<V> cacheObject) {
        if (System.currentTimeMillis() >= cacheObject.getExpireTime()) {
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
    public void putAll(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        HashMap m = new HashMap(map);
        for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
            CacheValueHolder<V> cacheObject = new CacheValueHolder(en.getValue(), System.currentTimeMillis(), timeUnit.toMillis(expire));
            m.put(buildKey(en.getKey()), cacheObject);
        }
        innerMap.putAllValues(m);
    }

    @Override
    public CacheResult REMOVE(K key) {
        innerMap.removeValue(buildKey(key));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        Set newKeys = keys.stream().map((key) -> buildKey(key)).collect(Collectors.toSet());
        innerMap.removeAllValues(newKeys);
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return SimpleLock.tryLock(this, buildKey(key), expire, timeUnit);
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expire, TimeUnit timeUnit) {
        CacheValueHolder<V> cacheObject = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
        if (innerMap.putIfAbsentValue(buildKey(key), cacheObject)) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } else {
            return CacheResult.EXISTS_WITHOUT_MSG;
        }
    }
}
