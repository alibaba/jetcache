package com.alicp.jetcache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created on 16/9/13.
 *
 * @author huangli
 */
public class MultiLevelCache<K, V> extends AbstractCache<K, V> {
    private Cache[] caches;

    private MultiLevelCacheConfig<K, V> config;

    @SuppressWarnings("unchecked")
    @Deprecated
    public MultiLevelCache(Cache... caches) throws CacheConfigException {
        this.caches = caches;
        checkCaches();
        CacheConfig lastConfig = caches[caches.length - 1].config();
        config = new MultiLevelCacheConfig<>();
        config.setCaches(Arrays.asList(caches));
        config.setCacheNullValue(lastConfig.isCacheNullValue());
    }

    @SuppressWarnings("unchecked")
    public MultiLevelCache(MultiLevelCacheConfig<K, V> cacheConfig) throws CacheConfigException {
        this.config = cacheConfig;
        this.caches = cacheConfig.getCaches().toArray(new Cache[]{});
        checkCaches();
    }

    private void checkCaches() {
        if (caches == null || caches.length == 0) {
            throw new IllegalArgumentException();
        }
        for (Cache c : caches) {
            if (c.config().getLoader() != null) {
                throw new CacheConfigException("Loader on sub cache is not allowed, set the loader into MultiLevelCache.");
            }
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
    public CacheResult PUT(K key, V value) {
        // let each level cache decide the expiration time
        return PUT(key, value, 0, null);
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        // let each level cache decide the expiration time
        return PUT_ALL(map, 0, null);
    }

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        for (int i = 0; i < caches.length; i++) {
            Cache cache = caches[i];
            CacheGetResult result = cache.GET(key);
            if (result.isSuccess()) {
                CacheValueHolder<V> holder = unwrapHolder(result.getHolder());
                checkResultAndFillUpperCache(key, i, holder);
                return new CacheGetResult(CacheResultCode.SUCCESS, null, holder);
            }
        }
        return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
    }

    private CacheValueHolder<V> unwrapHolder(CacheValueHolder<V> h) {
        // if @Cached or @CacheCache change type from REMOTE to BOTH (or from BOTH to REMOTE),
        // during the dev/publish process, the value type which different application server put into cache server will be different
        // (CacheValueHolder<V> and CacheValueHolder<CacheValueHolder<V>>, respectively).
        // So we need correct the problem at here and in CacheGetResult.
        Objects.requireNonNull(h);
        if (h.getValue() instanceof CacheValueHolder) {
            return (CacheValueHolder<V>) h.getValue();
        } else {
            return h;
        }
    }

    private void checkResultAndFillUpperCache(K key, int i, CacheValueHolder<V> h) {
        Objects.requireNonNull(h);
        long currentExpire = h.getExpireTime();
        long now = System.currentTimeMillis();
        if (now <= currentExpire) {
            /*
            原来这里有两种情况：
              - 如果 isUseExpireOfSubCache，那么让 Local 自己选择过期时间
              - 如果为 false，那么选择远程的剩余时间作为过期时间
            现在的代码，总是选择远程的剩余时间作为过期时间，但是增加了参数 isForce 来控制不能超过配置的本地缓存时间
            而对于用户直接 PUT_EXPIRED 则允许此类操作
             */
            long restTtl = currentExpire - now;
            if (restTtl > 0) {
                PUT_caches(i, key, h.getValue(), restTtl, TimeUnit.MILLISECONDS,false);
            }
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        HashMap<K, CacheGetResult<V>> resultMap = new HashMap<>();
        Set<K> restKeys = new HashSet<>(keys);
        for (int i = 0; i < caches.length; i++) {
            if (restKeys.size() == 0) {
                break;
            }
            Cache<K, CacheValueHolder<V>> c = caches[i];
            MultiGetResult<K, CacheValueHolder<V>> allResult = c.GET_ALL(restKeys);
            if (allResult.isSuccess() && allResult.getValues() != null) {
                for (Map.Entry<K, CacheGetResult<CacheValueHolder<V>>> en : allResult.getValues().entrySet()) {
                    K key = en.getKey();
                    CacheGetResult result = en.getValue();
                    if (result.isSuccess()) {
                        CacheValueHolder<V> holder = unwrapHolder(result.getHolder());
                        checkResultAndFillUpperCache(key, i, holder);
                        resultMap.put(key, new CacheGetResult(CacheResultCode.SUCCESS, null, holder));
                        restKeys.remove(key);
                    }
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
        return PUT_caches(caches.length, key, value, expireAfterWrite, timeUnit, true);
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        CompletableFuture<ResultData> future = CompletableFuture.completedFuture(null);
        for (Cache c : caches) {
            CacheResult r;
            if(timeUnit == null) {
                r = c.PUT_ALL(map);
            } else {
                r = c.PUT_ALL(map, expireAfterWrite, timeUnit);
            }
            future = combine(future, r);
        }
        return new CacheResult(future);
    }

    private CacheResult PUT_caches(int lastIndex, K key, V value, long expire, TimeUnit timeUnit, boolean isForce) {
        CompletableFuture<ResultData> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < lastIndex; i++) {
            Cache cache = caches[i];
            CacheResult r;
            if (timeUnit == null) {
                r = cache.PUT(key, value);
            } else {
                // 只有 isForce = 用户强制 && expire <= 本地缓存时间 才会使用参数的 expire
                if (isForce && expire <= cache.config().getExpireAfterWriteInMillis()) {
                    r = cache.PUT(key, value, expire, timeUnit);
                } else {
                    r = cache.PUT(key, value);
                }
            }
            future = combine(future, r);
        }
        return new CacheResult(future);
    }

    private CompletableFuture<ResultData> combine(CompletableFuture<ResultData> future, CacheResult result) {
        return future.thenCombine(result.future(), (d1, d2) -> {
            if (d1 == null) {
                return d2;
            }
            if (d1.getResultCode() != d2.getResultCode()) {
                return new ResultData(CacheResultCode.PART_SUCCESS, null, null);
            }
            return d1;
        });
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        CompletableFuture<ResultData> future = CompletableFuture.completedFuture(null);
        for (Cache cache : caches) {
            CacheResult r = cache.REMOVE(key);
            future = combine(future, r);
        }
        return new CacheResult(future);
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        CompletableFuture<ResultData> future = CompletableFuture.completedFuture(null);
        for (Cache cache : caches) {
            CacheResult r = cache.REMOVE_ALL(keys);
            future = combine(future, r);
        }
        return new CacheResult(future);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        for (Cache cache : caches) {
            try {
                T obj = (T) cache.unwrap(clazz);
                if (obj != null) {
                    return obj;
                }
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        throw new IllegalArgumentException(clazz.getName());
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

    @Override
    public void close() {
        super.close();
        for (Cache c : caches) {
            c.close();
        }
    }
}
