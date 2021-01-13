package com.alicp.jetcache;

import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import com.alicp.jetcache.event.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(AbstractCache.class);

    private volatile ConcurrentHashMap<Object, LoaderLock> loaderMap;

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
                .append(" error.");
        if (!(key instanceof byte[])) {
            try {
                sb.append(" key=[")
                        .append(config().getKeyConvertor().apply((K) key))
                        .append(']');
            } catch (Exception ex) {
                // ignore
            }
        }
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
        CacheGetResult<V> result;
        if (key == null) {
            result = new CacheGetResult<V>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        } else {
            result = do_GET(key);
        }
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
        MultiGetResult<K, V> result;
        if (keys == null) {
            result = new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        } else {
            result = do_GET_ALL(keys);
        }
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
        CacheLoader<K, V> newLoader = CacheUtil.createProxyLoader(cache, loader, abstractCache::notify);
        CacheGetResult<V> r;
        if (cache instanceof RefreshCache) {
            RefreshCache<K, V> refreshCache = ((RefreshCache<K, V>) cache);
            r = refreshCache.GET(key);
            refreshCache.addOrUpdateRefreshTask(key, newLoader);
        } else {
            r = cache.GET(key);
        }
        if (r.isSuccess()) {
            return r.getValue();
        } else {
            Consumer<V> cacheUpdater = (loadedValue) -> {
                if(needUpdate(loadedValue, cacheNullWhenLoaderReturnNull, newLoader)) {
                    if (timeUnit != null) {
                        cache.PUT(key, loadedValue, expireAfterWrite, timeUnit).waitForResult();
                    } else {
                        cache.PUT(key, loadedValue).waitForResult();
                    }
                }
            };

            V loadedValue;
            if (cache.config().isCachePenetrationProtect()) {
                loadedValue = synchronizedLoad(cache.config(), abstractCache, key, newLoader, cacheUpdater);
            } else {
                loadedValue = newLoader.apply(key);
                cacheUpdater.accept(loadedValue);
            }

            return loadedValue;
        }
    }

    static <K, V> V synchronizedLoad(CacheConfig config, AbstractCache<K,V> abstractCache,
                                     K key, Function<K, V> newLoader, Consumer<V> cacheUpdater) {
        ConcurrentHashMap<Object, LoaderLock> loaderMap = abstractCache.initOrGetLoaderMap();
        Object lockKey = buildLoaderLockKey(abstractCache, key);
        while (true) {
            boolean create[] = new boolean[1];
            LoaderLock ll = loaderMap.computeIfAbsent(lockKey, (unusedKey) -> {
                create[0] = true;
                LoaderLock loaderLock = new LoaderLock();
                loaderLock.signal = new CountDownLatch(1);
                loaderLock.loaderThread = Thread.currentThread();
                return loaderLock;
            });
            if (create[0] || ll.loaderThread == Thread.currentThread()) {
                try {
                    V loadedValue = newLoader.apply(key);
                    ll.success = true;
                    ll.value = loadedValue;
                    cacheUpdater.accept(loadedValue);
                    return loadedValue;
                } finally {
                    if (create[0]) {
                        ll.signal.countDown();
                        loaderMap.remove(lockKey);
                    }
                }
            } else {
                try {
                    Duration timeout = config.getPenetrationProtectTimeout();
                    if (timeout == null) {
                        ll.signal.await();
                    } else {
                        boolean ok = ll.signal.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        if(!ok) {
                            logger.info("loader wait timeout:" + timeout);
                            return newLoader.apply(key);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.warn("loader wait interrupted");
                    return newLoader.apply(key);
                }
                if (ll.success) {
                    return (V) ll.value;
                } else {
                    continue;
                }

            }
        }
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
        CacheResult result;
        if (key == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_PUT(key, value, expireAfterWrite, timeUnit);
        }
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
        CacheResult result;
        if (map == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_PUT_ALL(map, expireAfterWrite, timeUnit);
        }
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
        CacheResult result;
        if (key == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_REMOVE(key);
        }
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
        CacheResult result;
        if (keys == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_REMOVE_ALL(keys);
        }
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
        CacheResult result;
        if (key == null) {
            result = CacheResult.FAIL_ILLEGAL_ARGUMENT;
        } else {
            result = do_PUT_IF_ABSENT(key, value, expireAfterWrite, timeUnit);
        }
        result.future().thenRun(() -> {
            CachePutEvent event = new CachePutEvent(this, System.currentTimeMillis() - t, key, value, result);
            notify(event);
        });
        return result;
    }

    protected abstract CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);

    static class LoaderLock {
        CountDownLatch signal;
        Thread loaderThread;
        volatile boolean success;
        volatile Object value;
    }
}
