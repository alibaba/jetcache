package com.alicp.jetcache;

import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.JetCacheExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/5/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class RefreshCache<K, V> extends LoadingCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshCache.class);

    private ConcurrentHashMap<Object, Runnable> taskMap = new ConcurrentHashMap<>();

    public RefreshCache(Cache cache) {
        super(cache);
    }

    private boolean hasLoader() {
        return config.getLoader() != null || config.getBatchLoader() != null;
    }

    private Object getTaskId(K key) {
        Cache c = getTargetCache();
        while (true) {
            if (c instanceof ProxyCache) {
                c = ((ProxyCache) c).getTargetCache();
            } else if (c instanceof MultiLevelCache) {
                Cache[] caches = ((MultiLevelCache) c).caches();
                c = caches[caches.length - 1];
            } else {
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
        }
    }

    private void addTask(Object taskId, long refreshMillis, K key) {
        Cache cache = this;
        final byte[] suffix = "@Lock@".getBytes();
        Runnable command = () -> {
            try {
                Cache c = cache;
                while (c instanceof ProxyCache) {
                    c = ((ProxyCache) c).getTargetCache();
                }
                if (c instanceof AbstractExternalCache) {
                    byte[] newKey = ((AbstractExternalCache) c).buildKey(key);
                    byte[] lockKey = Arrays.copyOf(newKey, newKey.length + suffix.length);
                    System.arraycopy(suffix, 0, lockKey, newKey.length, suffix.length);
                    long loadTimeOut = config.getRefreshPolicy().getLoadLockTimeOutMillis();
                    Method method = cache.getClass().getMethod("tryLockAndRun",
                            Object.class, long.class, TimeUnit.class, Runnable.class);
                    Runnable r = () -> get(key);
                    method.invoke(cache, lockKey, loadTimeOut, TimeUnit.MILLISECONDS, r);
                } else {
                    get(key);
                }
            } catch (InvocationTargetException e) {
                logger.error("load key error: key=" + key, e.getTargetException());
            } catch (Throwable e) {
                logger.error("load key error: key=" + key, e);
            }
        };
        taskMap.computeIfAbsent(taskId, (tid) -> {
            JetCacheExecutor.defaultExecutor().scheduleWithFixedDelay(
                    command, refreshMillis, refreshMillis, TimeUnit.MILLISECONDS);
            return command;
        });
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        if (config.getRefreshPolicy() != null && hasLoader()) {
            addTask(getTaskId(key),
                    config.getRefreshPolicy().getRefreshMillis(),
                    key);
        }
        return super.GET(key);
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        if (config.getRefreshPolicy() != null && hasLoader()) {
            for (K key : keys) {
                addTask(getTaskId(key),
                        config.getRefreshPolicy().getRefreshMillis(),
                        key);
            }
        }
        return super.GET_ALL(keys);
    }
}
