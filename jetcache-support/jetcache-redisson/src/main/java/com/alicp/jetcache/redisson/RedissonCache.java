package com.alicp.jetcache.redisson;

import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.MultiGetResult;
import com.alicp.jetcache.external.AbstractExternalCache;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/7/12.
 *
 * @author <a href="mailto:jeason1914@qq.com">yangyong</a>
 */
public class RedissonCache<K, V> extends AbstractExternalCache<K, V> {
    private final RedissonClient client;
    private final RedissonCacheConfig<K, V> config;

    public RedissonCache(final RedissonCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.client = config.getRedissonClient();
    }

    protected String getCacheKey(final K key) {
        final byte[] newKey = buildKey(key);
        return new String(newKey, StandardCharsets.UTF_8);
    }

    @Override
    public CacheConfig<K, V> config() {
        return this.config;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        throw new UnsupportedOperationException("RedissonCache does not support unwrap");
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected CacheGetResult<V> do_GET(final K key) {
        try {
            final RBucket<CacheValueHolder<V>> rb = this.client.getBucket(getCacheKey(key));
            final CacheValueHolder<V> holder = rb.get();
            if (Objects.nonNull(holder)) {
                final long now = System.currentTimeMillis(), expire = holder.getExpireTime();
                if (expire > 0 && now >= expire) {
                    return CacheGetResult.EXPIRED_WITHOUT_MSG;
                }
                return new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder);
            }
            return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
        } catch (Throwable e) {
            logError("GET", key, e);
            return new CacheGetResult<>(e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected MultiGetResult<K, V> do_GET_ALL(final Set<? extends K> keys) {
        try {
            final Map<K, CacheGetResult<V>> retMap = new HashMap<>(1 << 4);
            if (Objects.nonNull(keys) && !keys.isEmpty()) {
                final Map<K, String> keyMap = new HashMap<>(keys.size());
                for (K k : keys) {
                    if (Objects.nonNull(k)) {
                        final String key = getCacheKey(k);
                        if (Objects.nonNull(key)) {
                            keyMap.put(k, key);
                        }
                    }
                }
                if (!keyMap.isEmpty()) {
                    final Map<String, Object> kvMap = this.client.getBuckets().get(keyMap.values().toArray(new String[0]));
                    final long now = System.currentTimeMillis();
                    for (K k : keys) {
                        final String key = keyMap.get(k);
                        if (Objects.nonNull(key) && Objects.nonNull(kvMap)) {
                            final CacheValueHolder<V> holder = (CacheValueHolder<V>) kvMap.get(key);
                            if (Objects.nonNull(holder)) {
                                final long expire = holder.getExpireTime();
                                final CacheGetResult<V> ret = (expire > 0 && now >= expire) ? CacheGetResult.EXPIRED_WITHOUT_MSG :
                                        new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder);
                                retMap.put(k, ret);
                                continue;
                            }
                        }
                        retMap.put(k, CacheGetResult.NOT_EXISTS_WITHOUT_MSG);
                    }
                }
            }
            return new MultiGetResult<>(CacheResultCode.SUCCESS, null, retMap);
        } catch (Throwable e) {
            logError("GET_ALL", "keys(" + (Objects.nonNull(keys) ? keys.size() : 0) + ")", e);
            return new MultiGetResult<>(e);
        }
    }

    @Override
    protected CacheResult do_PUT(final K key, final V value, final long expireAfterWrite, final TimeUnit timeUnit) {
        try {
            final CacheValueHolder<V> holder = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
            this.client.getBucket(getCacheKey(key)).set(holder, expireAfterWrite, timeUnit);
            return CacheGetResult.SUCCESS_WITHOUT_MSG;
        } catch (Throwable e) {
            logError("PUT", key, e);
            return new CacheResult(e);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(final Map<? extends K, ? extends V> map, final long expireAfterWrite, final TimeUnit timeUnit) {
        try {
            if (Objects.nonNull(map) && !map.isEmpty()) {
                final long expire = timeUnit.toMillis(expireAfterWrite);
                final RBatch batch = this.client.createBatch();
                map.forEach((k, v) -> {
                    final CacheValueHolder<V> holder = new CacheValueHolder<>(v, expire);
                    batch.getBucket(getCacheKey(k)).setAsync(holder, expireAfterWrite, timeUnit);
                });
                batch.execute();
            }
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Throwable e) {
            logError("PUT_ALL", "map(" + map.size() + ")", e);
            return new CacheResult(e);
        }
    }

    @Override
    protected CacheResult do_REMOVE(final K key) {
        try {
            final boolean ret = this.client.getBucket(getCacheKey(key)).delete();
            return ret ? CacheResult.SUCCESS_WITHOUT_MSG : CacheResult.FAIL_WITHOUT_MSG;
        } catch (Throwable e) {
            logError("REMOVE", key, e);
            return new CacheResult(e);
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(final Set<? extends K> keys) {
        try {
            if (Objects.nonNull(keys) && !keys.isEmpty()) {
                final RBatch batch = this.client.createBatch();
                keys.forEach(key -> batch.getBucket(getCacheKey(key)).deleteAsync());
                batch.execute();
            }
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Throwable e) {
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", e);
            return new CacheResult(e);
        }
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(final K key, final V value, final long expireAfterWrite, final TimeUnit timeUnit) {
        try {
            final CacheValueHolder<V> holder = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
            final boolean success = this.client.getBucket(getCacheKey(key)).trySet(holder, expireAfterWrite, timeUnit);
            return success ? CacheResult.SUCCESS_WITHOUT_MSG : CacheResult.EXISTS_WITHOUT_MSG;
        } catch (Throwable e) {
            logError("PUT_IF_ABSENT", key, e);
            return new CacheResult(e);
        }
    }
}