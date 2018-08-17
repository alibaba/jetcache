package com.alicp.jetcache;

import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import com.alicp.jetcache.event.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(AbstractCache.class);

    private ConcurrentHashMap<Object, LoaderLock> loaderMap;


    ConcurrentHashMap<Object, LoaderLock> initOrGetLoaderMap() {
        if (loaderMap == null) {
            synchronized (this) {
                if (loaderMap == null) {
                    loaderMap = new ConcurrentHashMap<>();
                }
            }
        }
        return loaderMap;
    }

    protected void logError(String oper, Object key, Throwable e) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("jetcache(")
                .append(this.getClass().getSimpleName()).append(") ")
                .append(oper)
                .append(" error. key=")
                .append(FastjsonKeyConvertor.INSTANCE.apply(key))
                .append(".");
        if (needLogStackTrace(e)) {
            logger.error(sb.toString(), e);
        } else {
            sb.append(' ');
            while (e != null) {
                sb.append(e.getClass().getName());
                sb.append(':');
                sb.append(e.getMessage());
                e = e.getCause();
                if (e != null) {
                    sb.append("\ncause by ");
                }
            }
            logger.error(sb.toString());
        }

    }

    protected boolean needLogStackTrace(Throwable e) {
//        if (e instanceof CacheEncodeException) {
//            return true;
//        }
//        return false;
        return true;
    }

    public void notify(CacheEvent e) {
        List<CacheMonitor> monitors = config().getMonitors();
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

    protected abstract CacheGetResult<V> do_GET(K key);

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

    protected abstract MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys);

    @Override
    public final V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull) {
        return computeIfAbsentImpl(key, loader, cacheNullWhenLoaderReturnNull,
                0, null, this);
    }

    @Override
    public final V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull,
                                   long expireAfterWrite, TimeUnit timeUnit) {
        return computeIfAbsentImpl(key, loader, cacheNullWhenLoaderReturnNull,
                expireAfterWrite, timeUnit, this);
    }

    private static <K, V> boolean needUpdate(V loadedValue, boolean cacheNullWhenLoaderReturnNull, Function<K, V> loader) {
        if (loadedValue == null && !cacheNullWhenLoaderReturnNull) {
            return false;
        }
        if (loader instanceof CacheLoader && ((CacheLoader<K, V>) loader).vetoCacheUpdate()) {
            return false;
        }
        return true;
    }

    static <K, V> V computeIfAbsentImpl(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull,
                                               long expireAfterWrite, TimeUnit timeUnit, Cache<K, V> cache) {
        AbstractCache<K, V> abstractCache = CacheUtil.getAbstractCache(cache);
        Function<K, V> newLoader = CacheUtil.createProxyLoader(cache, loader, abstractCache::notify);
        CacheGetResult<V> r = cache.GET(key);
        if (r.isSuccess()) {
            return r.getValue();
        } else {
            Consumer<V> cacheUpdater = (loadedValue) -> {
                if(needUpdate(loadedValue, cacheNullWhenLoaderReturnNull, loader)) {
                    if (timeUnit != null) {
                        cache.PUT(key, loadedValue, expireAfterWrite, timeUnit);
                    } else {
                        cache.put(key, loadedValue);
                    }
                }
            };

            V loadedValue;
            if (cache.config().isCachePenetrationProtect()) {
                ConcurrentHashMap<Object, LoaderLock> loaderMap = abstractCache.initOrGetLoaderMap();
                loadedValue = synchronizedLoad(abstractCache, key, newLoader, cacheUpdater, loaderMap);
            } else {
                loadedValue = newLoader.apply(key);
                cacheUpdater.accept(loadedValue);
            }

            return loadedValue;
        }
    }

    static <K, V> Map<K, V> synchronizedLoad(Cache<K,V> abstractCache, Set<K> keys, Function<Set<K>, Map<K, V>> newLoader,
                                             Consumer<Map<K, V>> cacheUpdater,
                                             ConcurrentHashMap<Object, LoaderLock> loaderMap) {
        Map<K, V> loadedValues = new HashMap<>();

        // value is [lockKey, LoaderLock]
        Map<K, Map.Entry<Object, LoaderLock>> lockMap = new HashMap<>();

        for (K key : keys) {
            Object lockKey = buildLoaderLockKey(abstractCache, key);

            while (true) {
                boolean create[] = new boolean[1];
                LoaderLock ll = loaderMap.computeIfAbsent(lockKey, (unusedKey) -> {
                    create[0] = true;
                    LoaderLock loaderLock = new LoaderLock();
                    return loaderLock;
                });
                if (create[0]) {
                    // collect need load keys
                    lockMap.put(key, new AbstractMap.SimpleEntry<>(lockKey, ll));
                } else {
                    try {
                        ll.signal.await();
                        if (ll.success) {
                            loadedValues.put(key, (V) ll.value);
                            break;
                        } else {
                            continue;
                        }
                    } catch (InterruptedException e) {
                        throw new CacheException("loader wait interrupted", e);
                    }
                }
            }
        }

        if (!lockMap.isEmpty()) {

            try {
                Map<K, V> newLoadedValues = newLoader.apply(lockMap.keySet());

                for (Map.Entry<K, Map.Entry<Object, LoaderLock>> lockEntry : lockMap.entrySet()) {
                    lockEntry.getValue().getValue().success = true;
                    lockEntry.getValue().getValue().value = newLoadedValues.get(lockEntry.getKey());
                }

                loadedValues.putAll(newLoadedValues);
                cacheUpdater.accept(newLoadedValues);
            } finally {
                for (Map.Entry<K, Map.Entry<Object, LoaderLock>> lockEntry : lockMap.entrySet()) {
                    loaderMap.remove(lockEntry.getValue().getKey());
                    lockEntry.getValue().getValue().signal.countDown();
                }
            }

        }

        return loadedValues;
    }

    static <K, V> V synchronizedLoad(Cache<K,V> abstractCache, K key, Function<K, V> newLoader,
                                     Consumer<V> cacheUpdater,
                                     ConcurrentHashMap<Object, LoaderLock> loaderMap) {
        V loadedValue;
        Object lockKey = buildLoaderLockKey(abstractCache, key);
        while (true) {
            boolean create[] = new boolean[1];
            LoaderLock ll = loaderMap.computeIfAbsent(lockKey, (unusedKey) -> {
                create[0] = true;
                LoaderLock loaderLock = new LoaderLock();
                return loaderLock;
            });
            if (create[0]) {
                try {
                    loadedValue = newLoader.apply(key);
                    ll.success = true;
                    ll.value = loadedValue;
                    cacheUpdater.accept(loadedValue);
                    break;
                } finally {
                    loaderMap.remove(lockKey);
                    ll.signal.countDown();
                }
            } else {
                try {
                    ll.signal.await();
                    if (ll.success) {
                        loadedValue = (V) ll.value;
                        break;
                    } else {
                        continue;
                    }
                } catch (InterruptedException e) {
                    throw new CacheException("loader wait interrupted", e);
                }
            }
        }
        return loadedValue;
    }

    private static Object buildLoaderLockKey(Cache c, Object key) {
        if (c instanceof AbstractEmbeddedCache) {
            return ((AbstractEmbeddedCache) c).buildKey(key);
        } else if (c instanceof AbstractExternalCache) {
            byte bytes[] = ((AbstractExternalCache) c).buildKey(key);
            return ByteBuffer.wrap(bytes);
        } else if (c instanceof MultiLevelCache) {
            c = ((MultiLevelCache) c).caches()[0];
            return buildLoaderLockKey(c, key);
        } else if(c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
            return buildLoaderLockKey(c, key);
        } else {
            throw new CacheException("impossible");
        }
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

    protected abstract CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

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

    protected abstract CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit);

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

    protected abstract CacheResult do_REMOVE(K key);

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

    protected abstract CacheResult do_REMOVE_ALL(Set<? extends K> keys);

    @Override
    public final CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        long t = System.currentTimeMillis();
        CacheResult result = do_PUT_IF_ABSENT(key, value, expireAfterWrite, timeUnit);
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(this, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    protected abstract CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

    static class LoaderLock {
        CountDownLatch signal = new CountDownLatch(1);
        boolean success;
        Object value;
    }
}
