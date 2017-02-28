/**
 * Created on  13-10-17 23:01
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;

import java.util.ArrayList;
import java.util.HashMap;
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
        if (key == null) {
            return null;
        }
        Object newKey = key;
        Function<Object, Object> keyConvertor = config.getKeyConvertor();
        if (keyConvertor != null) {
            newKey = keyConvertor.apply(key);
        }
        return newKey;
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        if (key == null) {
            return new CacheGetResult<V>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        Object newKey = buildKey(key);
        CacheValueHolder<V> holder = (CacheValueHolder<V>) innerMap.getValue(newKey);
        return getImpl(newKey, holder);
    }

    private CacheGetResult<V> getImpl(Object newKey, CacheValueHolder<V> holder) {
        if (holder == null) {
            return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
        } else if (System.currentTimeMillis() >= holder.getExpireTime()) {
            // innerMap.removeValue(newKey);
            return CacheGetResult.EXPIRED_WITHOUT_MSG;
        } else {
            if (config.isExpireAfterAccess()) {
                long ttlInMillis = holder.getInitTtlInMillis();
                holder.setExpireTime(System.currentTimeMillis() + ttlInMillis);
            }
            return new CacheGetResult(CacheResultCode.SUCCESS, null, holder.getValue());
        }
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        ArrayList<K> keyList = new ArrayList<K>(keys.size());
        ArrayList<Object> newKeyList = new ArrayList<Object>(keys.size());
        keys.stream().forEach((k) -> {
            Object newKey = buildKey(k);
            keyList.add(k);
            newKeyList.add(newKey);
        });
        Map<Object, CacheValueHolder<V>> innerResultMap = innerMap.getAllValues(newKeyList);
        Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            K key = keyList.get(i);
            Object newKey = newKeyList.get(i);
            CacheValueHolder<V> holder = innerResultMap.get(newKey);
            resultMap.put(key, getImpl(newKey, holder));
        }
        MultiGetResult<K, V> result = new MultiGetResult<>(CacheResultCode.SUCCESS, null, resultMap);
        return result;
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        CacheValueHolder<V> cacheObject = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
        innerMap.putValue(buildKey(key), cacheObject);
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        HashMap newKeyMap = new HashMap();
        for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
            CacheValueHolder<V> cacheObject = new CacheValueHolder(en.getValue(), System.currentTimeMillis(), timeUnit.toMillis(expire));
            newKeyMap.put(buildKey(en.getKey()), cacheObject);
        }
        innerMap.putAllValues(newKeyMap);

        final HashMap resultMap = new HashMap();
        map.keySet().forEach((k) -> resultMap.put(k, CacheResultCode.SUCCESS));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public CacheResult REMOVE(K key) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        innerMap.removeValue(buildKey(key));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public CacheResult REMOVE_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        Set newKeys = keys.stream().map((key) -> buildKey(key)).collect(Collectors.toSet());
        innerMap.removeAllValues(newKeys);

        final HashMap resultMap = new HashMap();
        keys.forEach((k) -> resultMap.put(k, CacheResultCode.SUCCESS));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return null;
        }
        return SimpleLock.tryLock(this, buildKey(key), expire, timeUnit);
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        CacheValueHolder<V> cacheObject = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
        if (innerMap.putIfAbsentValue(buildKey(key), cacheObject)) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } else {
            return CacheResult.EXISTS_WITHOUT_MSG;
        }
    }
}
