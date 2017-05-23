package com.alicp.jetcache;

import com.alicp.jetcache.event.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCache<K, V> implements Cache<K, V> {

    private ConfigAwareCache[] caches;

    private List<CacheMonitor> monitors = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public MultiLevelCache(Cache... caches) {
        this.caches = new ConfigAwareCache[caches.length];
        for (int i = 0; i < caches.length; i++) {
            Cache c = caches[i];
            if (!(c instanceof ConfigAwareCache)) {
                throw new CacheException("need ConfigAwareCache instance, but is " + c);
            } else {
                this.caches[i] = (ConfigAwareCache) c;
            }
        }
    }

    public Cache[] caches() {
        return caches;
    }

    public List<CacheMonitor> getMonitors() {
        return monitors;
    }

    public void notify(CacheEvent e) {
        for (CacheMonitor m : monitors) {
            m.afterOperation(e);
        }
    }

    @Override
    public final CacheGetResult<V> GET(K key) {
        long t = System.currentTimeMillis();
        CacheGetResult<V> result = do_GET(key);
        result.future().thenRun(() -> {
            CacheGetEvent event = new CacheGetEvent(this, System.currentTimeMillis() - t, key, result);
            notify(event);
        });
        return result;
    }

    private CacheGetResult<V> do_GET(K key) {
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
    public final MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        MultiGetResult<K, V> result = do_GET_ALL(keys);
        result.future().thenRun(() -> {
            CacheGetAllEvent event = new CacheGetAllEvent(this, System.currentTimeMillis() - t, keys, result);
            notify(event);
        });
        return result;
    }

    private MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
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
    public final V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        Function<K, V> newLoader = CacheUtil.createProxyLoader(this, key, loader, this::notify);
        return Cache.super.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull);
    }

    @Override
    public final V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expireAfterWrite, TimeUnit timeUnit) {
        Function<K, V> newLoader = CacheUtil.createProxyLoader(this, key, loader, this::notify);
        return Cache.super.computeIfAbsent(key, newLoader, cacheNullWhenLoaderReturnNull, expireAfterWrite, timeUnit);
    }

    @Override
    public final CacheResult PUT(K key, V value) {
        long t = System.currentTimeMillis();
        CacheResult result = do_PUT(key, value);
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(this, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    private CacheResult do_PUT(K key, V value) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT_caches(true, caches.length, key, value, Integer.MIN_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public final CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = do_PUT(key, value, expireAfterWrite, timeUnit);
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(this, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    private CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return PUT_caches(false, caches.length, key, value, expireAfterWrite, timeUnit);
    }

    @Override
    public final CacheResult PUT_ALL(Map<? extends K, ? extends V> map) {
        long t = System.currentTimeMillis();
        CacheResult result = do_PUT_ALL(map);
        result.future().thenRun(() -> {
            CachePutAllEvent event = new CachePutAllEvent(this, System.currentTimeMillis() - t, map, result);
            notify(event);
        });
        return result;
    }

    private CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map) {
        return PUT_ALL_impl(true, map, Integer.MIN_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public final CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = do_PUT_ALL(map, expireAfterWrite, timeUnit);
        result.future().thenRun(() -> {
            CachePutAllEvent event = new CachePutAllEvent(this, System.currentTimeMillis() - t, map, result);
            notify(event);
        });
        return result;
    }

    private CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        return PUT_ALL_impl(false, map, expireAfterWrite, timeUnit);
    }

    private CacheResult PUT_ALL_impl(boolean useDefaultExpire,
                                     Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        int failCount = 0;
        for (ConfigAwareCache c : caches) {
            Map newMap = new HashMap();
            if (useDefaultExpire) {
                expire = c.config().getExpireAfterWriteInMillis();
                timeUnit = TimeUnit.MILLISECONDS;
            }
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                CacheValueHolder<V> h = new CacheValueHolder<>(en.getValue(), timeUnit.toMillis(expire));
                newMap.put(en.getKey(), h);
            }

            CacheResult r;
            if (useDefaultExpire) {
                r = c.PUT_ALL(newMap);
            } else {
                r = c.PUT_ALL(newMap, expire, timeUnit);
            }
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
            ConfigAwareCache cache = caches[i];
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
    public final CacheResult REMOVE(K key) {
        long t = System.currentTimeMillis();
        CacheResult result = do_REMOVE(key);
        result.future().thenRun(() -> {
            CacheRemoveEvent event = new CacheRemoveEvent(this, System.currentTimeMillis() - t, key, result);
            notify(event);
        });
        return result;
    }

    private CacheResult do_REMOVE(K key) {
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
    public final CacheResult REMOVE_ALL(Set<? extends K> keys) {
        long t = System.currentTimeMillis();
        CacheResult result = do_REMOVE_ALL(keys);
        result.future().thenRun(() -> {
            CacheRemoveAllEvent event = new CacheRemoveAllEvent(this, System.currentTimeMillis() - t, keys, result);
            notify(event);
        });
        return result;
    }

    private CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
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
    public CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("PUT_IF_ABSENT is not supported by MultiLevelCache");
    }
}
