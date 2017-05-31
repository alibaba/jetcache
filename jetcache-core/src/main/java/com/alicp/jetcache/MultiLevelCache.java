package com.alicp.jetcache;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCache<K, V> extends AbstractCache<K, V> {

    private Cache[] caches;

    private MultiLevelCacheConfig<K, V> config;

    @SuppressWarnings("unchecked")
    @Deprecated
    public MultiLevelCache(Cache... caches) {
        this.caches = caches;
        if (caches == null || caches.length == 0) {
            throw new IllegalArgumentException();
        }
        CacheConfig lastConfig = caches[caches.length - 1].config();
        config = new MultiLevelCacheConfig<>();
        config.setCaches(Arrays.asList(caches));
        config.setExpireAfterAccessInMillis(lastConfig.getExpireAfterAccessInMillis());
        config.setExpireAfterWriteInMillis(lastConfig.getExpireAfterWriteInMillis());
        config.setCacheNullValue(lastConfig.isCacheNullValue());
    }

    @SuppressWarnings("unchecked")
    public MultiLevelCache(MultiLevelCacheConfig<K,V> cacheConfig) {
        this.config = cacheConfig;
        this.caches = cacheConfig.getCaches().toArray(new Cache[]{});
        if (caches == null || caches.length == 0) {
            throw new IllegalArgumentException();
        }
    }

    public Cache[] caches() {
        return caches;
    }

    @Override
    public MultiLevelCacheConfig<K, V> config() {
        return config;
    }

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        if (key == null) {
            return new CacheGetResult<V>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        for (int i = 0; i < caches.length; i++) {
            Cache cache = caches[i];
            CacheValueHolder<V> h = (CacheValueHolder<V>) cache.get(key);
            if (checkResultAndFillUpperCache(key, i, h))
                return new CacheGetResult(CacheResultCode.SUCCESS, null, h.getValue());
        }
        return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
    }

    private boolean checkResultAndFillUpperCache(K key, int i, CacheValueHolder<V> h) {
        if (h != null) {
            long currentExpire = h.getExpireTime();
            long now = System.currentTimeMillis();
            if (now <= currentExpire) {
                long restTtl = currentExpire - now;
                if (restTtl > 0) {
                    PUT_caches(false, i, key, h.getValue(), restTtl, TimeUnit.MILLISECONDS);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        HashMap<K, CacheGetResult<V>> resultMap = new HashMap<>();
        Set<K> restKeys = new HashSet<K>(keys);
        for (int i = 0; i < caches.length; i++) {
            if (restKeys.size() == 0) {
                break;
            }
            Cache<K, CacheValueHolder<V>> c = caches[i];
            Map<K, CacheValueHolder<V>> someResult = c.getAll(restKeys);
            for (Map.Entry<K, CacheValueHolder<V>> en : someResult.entrySet()) {
                K key = en.getKey();
                CacheValueHolder<V> holder = en.getValue();
                if (checkResultAndFillUpperCache(key, i, holder)) {
                    resultMap.put(key, new CacheGetResult<V>(CacheResultCode.SUCCESS, null, holder.getValue()));
                    restKeys.remove(key);
                }
            }
        }
        for (K k : restKeys) {
            resultMap.put(k, CacheGetResult.NOT_EXISTS_WITHOUT_MSG);
        }
        return new MultiGetResult<>(CacheResultCode.SUCCESS, null, resultMap);
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT_caches(false, caches.length, key, value, expireAfterWrite, timeUnit);
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        int failCount = 0;
        for (Cache c : caches) {
            Map newMap = new HashMap();
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                CacheValueHolder<V> h = new CacheValueHolder<>(en.getValue(), timeUnit.toMillis(expireAfterWrite));
                newMap.put(en.getKey(), h);
            }

            CacheResult r = c.PUT_ALL(newMap, expireAfterWrite, timeUnit);
            if (!r.isSuccess()) {
                failCount++;
            }
        }
        return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                failCount == caches.length ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
    }

    private CacheResult PUT_caches(boolean useDefaultExpire, int lastIndex, K key, V value, long expire, TimeUnit timeUnit) {
        int failCount = 0;
        for (int i = 0; i < lastIndex; i++) {
            Cache cache = caches[i];
            if (useDefaultExpire) {
                expire = cache.config().getExpireAfterWriteInMillis();
                timeUnit = TimeUnit.MILLISECONDS;
            }
            CacheValueHolder<V> h = new CacheValueHolder<>(value, timeUnit.toMillis(expire));
            CacheResult r;
            if (useDefaultExpire) {
                r = cache.PUT(key, h);
            } else {
                r = cache.PUT(key, h, expire, timeUnit);
            }
            if (!r.isSuccess()) {
                failCount++;
            }
        }
        return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                failCount == caches.length ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        int failCount = 0;
        for (Cache cache : caches) {
            CacheResult r = cache.REMOVE(key);
            if (!r.isSuccess()) {
                failCount++;
            }
        }
        return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                failCount == caches.length ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        int failCount = 0;
        for (Cache cache : caches) {
            CacheResult r = cache.REMOVE_ALL(keys);
            if (!r.isSuccess()) {
                failCount++;
            }
        }
        return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                failCount == caches.length ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        throw new UnsupportedOperationException("unwrap is not supported by MultiLevelCache");
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return null;
        }
        return caches[caches.length - 1].tryLock(key, expire, timeUnit);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("putIfAbsent is not supported by MultiLevelCache");
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("PUT_IF_ABSENT is not supported by MultiLevelCache");
    }
}
