package com.alicp.jetcache;

import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.JetCacheExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/5/25.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RefreshCache<K, V> extends LoadingCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshCache.class);

    private ConcurrentHashMap<Object, RefreshTask> taskMap = new ConcurrentHashMap<>();

    private static Method tryLockAndRunMethod;
    private static Method getMethod;
    private static Method putMethod;

    static {
        try {
            tryLockAndRunMethod = Cache.class.getMethod("tryLockAndRun",
                    Object.class, long.class, TimeUnit.class, Runnable.class);
            getMethod = Cache.class.getMethod("GET", Object.class);
            putMethod = Cache.class.getMethod("put", Object.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new CacheException(e);
        }
    }

    public RefreshCache(Cache cache) {
        super(cache);
    }

    protected void stopRefresh() {
        List<RefreshTask> tasks = new ArrayList<>();
        tasks.addAll(taskMap.values());
        tasks.forEach(task -> task.cancel());
    }

    @Override
    public void close() {
        stopRefresh();
        super.close();
    }


    private boolean hasLoader() {
        return config.getLoader() != null;
    }

    protected Cache concreteCache() {
        Cache c = getTargetCache();
        while (true) {
            if (c instanceof ProxyCache) {
                c = ((ProxyCache) c).getTargetCache();
            } else if (c instanceof MultiLevelCache) {
                Cache[] caches = ((MultiLevelCache) c).caches();
                c = caches[caches.length - 1];
            } else {
                return c;
            }
        }
    }

    private boolean isMultiLevelCache() {
        Cache c = getTargetCache();
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return c instanceof MultiLevelCache;
    }

    private Object getTaskId(K key) {
        Cache c = concreteCache();
        if (c instanceof AbstractEmbeddedCache) {
            return ((AbstractEmbeddedCache) c).buildKey(key);
        } else if (c instanceof AbstractExternalCache) {
            byte[] bs = ((AbstractExternalCache) c).buildKey(key);
            return ByteBuffer.wrap(bs);
        } else {
            logger.error("can't getTaskId from " + c.getClass());
            return null;
        }
    }

    protected void addOrUpdateRefreshTask(K key, CacheLoader<K,V> loader) {
        RefreshPolicy refreshPolicy = config.getRefreshPolicy();
        if (refreshPolicy == null) {
            return;
        }
        long refreshMillis = refreshPolicy.getRefreshMillis();
        if (refreshMillis > 0) {
            Object taskId = getTaskId(key);
            RefreshTask refreshTask = taskMap.computeIfAbsent(taskId, tid -> {
                logger.debug("add refresh task. interval={},  key={}", refreshMillis , key);
                RefreshTask task = new RefreshTask(taskId, key, loader);
                task.lastAccessTime = System.currentTimeMillis();
                ScheduledFuture<?> future = JetCacheExecutor.heavyIOExecutor().scheduleWithFixedDelay(
                        task, refreshMillis, refreshMillis, TimeUnit.MILLISECONDS);
                task.future = future;
                return task;
            });
            refreshTask.lastAccessTime = System.currentTimeMillis();
        }
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        if (config.getRefreshPolicy() != null && hasLoader()) {
            addOrUpdateRefreshTask(key, null);
        }
        return cache.GET(key);
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        if (config.getRefreshPolicy() != null && hasLoader()) {
            for (K key : keys) {
                addOrUpdateRefreshTask(key, null);
            }
        }
        return cache.GET_ALL(keys);
    }

    class RefreshTask implements Runnable {
        private Object taskId;
        private K key;
        private CacheLoader<K, V> loader;

        private long lastAccessTime;
        private ScheduledFuture future;

        RefreshTask(Object taskId, K key, CacheLoader<K, V> loader) {
            this.taskId = taskId;
            this.key = key;
            this.loader = loader;
        }

        private void cancel() {
            logger.debug("cancel refresh: {}", key);
            future.cancel(false);
            taskMap.remove(taskId);
        }

        private void load() throws Throwable {
            CacheLoader<K,V> l = loader == null? config.getLoader(): loader;
            if (l != null) {
                l = CacheUtil.createProxyLoader(cache, l, eventConsumer);
                V v = l.load(key);
                if (v != null || config.isCacheNullValue()) {
                    cache.PUT(key, v);
                }
            }
        }

        private void externalLoad(final Cache concreteCache, final long currentTime)
                throws Throwable {
            byte[] newKey = ((AbstractExternalCache) concreteCache).buildKey(key);
            byte[] lockKey = combine(newKey, "_#RL#".getBytes());
            boolean isMultiLevelCache = isMultiLevelCache();
            long loadTimeOut = RefreshCache.this.config.getRefreshPolicy().getRefreshLockTimeoutMillis();
            long refreshMillis = config.getRefreshPolicy().getRefreshMillis();
            byte[] timestampKey = combine(newKey, "_#TS#".getBytes());

            // AbstractExternalCache buildKey method will not convert byte[]
            CacheGetResult refreshTimeResult = (CacheGetResult) getMethod.invoke(concreteCache, timestampKey);
            boolean shouldLoad = false;
            if (refreshTimeResult.isSuccess()) {
                shouldLoad = currentTime >= Long.parseLong(refreshTimeResult.getValue().toString()) + refreshMillis;
            } else if (refreshTimeResult.getResultCode() == CacheResultCode.NOT_EXISTS) {
                shouldLoad = true;
            }

            if (!shouldLoad) {
                if (isMultiLevelCache) refreshUpperCaches(key);
                return;
            }

            Runnable r = () -> {
                try {
                    load();
                    putMethod.invoke(concreteCache, timestampKey, String.valueOf(System.currentTimeMillis()));
                } catch (Throwable e) {
                    throw new CacheException("refresh error", e);
                }
            };
            tryLockAndRunMethod.invoke(concreteCache, lockKey, loadTimeOut, TimeUnit.MILLISECONDS, r);
            // no need to refreshUpperCaches when tryLock failed because of the remote cache value mostly has not been refreshed.
        }

        private void refreshUpperCaches(K key) {
            MultiLevelCache<K, V> targetCache = (MultiLevelCache<K, V>) getTargetCache();
            Cache[] caches = targetCache.caches();
            int len = caches.length;

            CacheGetResult cacheGetResult = caches[len - 1].GET(key);
            cacheGetResult.future().thenRun(() -> {
                if (!cacheGetResult.isSuccess()) return;

                for (int i = 0; i < len - 1; i++) {
                    caches[i].PUT(key, cacheGetResult.getValue());
                }
            });
        }

        @Override
        public void run() {
            try {
                if (config.getRefreshPolicy() == null || (loader == null && !hasLoader())) {
                    cancel();
                    return;
                }
                long now = System.currentTimeMillis();
                long stopRefreshAfterLastAccessMillis = config.getRefreshPolicy().getStopRefreshAfterLastAccessMillis();
                if (stopRefreshAfterLastAccessMillis > 0) {
                    if (lastAccessTime + stopRefreshAfterLastAccessMillis < now) {
                        logger.debug("cancel refresh: {}", key);
                        cancel();
                        return;
                    }
                }
                logger.debug("refresh key: {}", key);
                Cache concreteCache = concreteCache();
                if (concreteCache instanceof AbstractExternalCache) {
                    externalLoad(concreteCache, now);
                } else {
                    load();
                }
            } catch (Throwable e) {
                logger.error("refresh error: key=" + key, e);
            }
        }
    }

    private byte[] combine(byte[] bs1, byte[] bs2) {
        byte[] newArray = Arrays.copyOf(bs1, bs1.length + bs2.length);
        System.arraycopy(bs2, 0, newArray, bs1.length, bs2.length);
        return newArray;
    }
}
