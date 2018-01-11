package com.alicp.jetcache;

import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.JetCacheExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
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

    private void addTaskOrUpdateLastAccessTime(Object taskId, long refreshMillis, K key) {
        if (refreshMillis > 0 && taskId != null) {
            RefreshTask refreshTask = taskMap.computeIfAbsent(taskId, tid -> {
                RefreshTask task = new RefreshTask(taskId, key);
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
            addTaskOrUpdateLastAccessTime(getTaskId(key),
                    config.getRefreshPolicy().getRefreshMillis(),
                    key);
        }
        return cache.GET(key);
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        if (config.getRefreshPolicy() != null && hasLoader()) {
            for (K key : keys) {
                addTaskOrUpdateLastAccessTime(getTaskId(key),
                        config.getRefreshPolicy().getRefreshMillis(),
                        key);
            }
        }
        return cache.GET_ALL(keys);
    }

    class RefreshTask implements Runnable {
        private Object taskId;
        private K key;
        private long lastAccessTime;
        private ScheduledFuture future;

        RefreshTask(Object taskId, K key) {
            this.taskId = taskId;
            this.key = key;
        }

        private void cancel() {
            future.cancel(false);
            taskMap.remove(taskId);
        }

        private void load() throws Throwable {
            logger.debug("refresh {}", key);
            CacheLoader<K, V> loader = config.getLoader();
            loader = CacheUtil.createProxyLoader(cache, loader, eventConsumer);
            V v = loader.load(key);
            cache.PUT(key, v);
        }

        private void externalLoad(final Cache concreteCache, final long currentTime)
                throws Throwable {
            byte[] newKey = ((AbstractExternalCache) concreteCache).buildKey(key);
            byte[] lockKey = combine(newKey, "_#RL#".getBytes());
            long loadTimeOut = RefreshCache.this.config.getRefreshPolicy().getRefreshLockTimeoutMillis();
            Runnable r = () -> {
                try {
                    long refreshMillis = config.getRefreshPolicy().getRefreshMillis();
                    byte[] timestampKey = combine(newKey, "_#TS#".getBytes());
                    // AbstractExternalCache buildKey method will not convert byte[]
                    CacheGetResult refreshTimeResult = (CacheGetResult) getMethod.invoke(concreteCache, timestampKey);
                    if (refreshTimeResult.isSuccess() && (currentTime < Long.parseLong(refreshTimeResult.getValue().toString()) + refreshMillis)) {
                        return;
                    }
                    load();
                    // AbstractExternalCache buildKey method will not convert byte[]
                    putMethod.invoke(concreteCache, timestampKey, String.valueOf(System.currentTimeMillis()));
                } catch (Throwable e){
                    throw new CacheException("refresh error", e);
                }
            };
            // AbstractExternalCache buildKey method will not convert byte[]
            tryLockAndRunMethod.invoke(concreteCache, lockKey, loadTimeOut, TimeUnit.MILLISECONDS, r);
        }

        @Override
        public void run() {
            try {
                if (config.getRefreshPolicy() == null || config.getLoader() == null) {
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
