package com.alicp.jetcache.redisson;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.CacheEncodeException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2022/7/12.
 *
 * @author <a href="mailto:jeason1914@qq.com">yangyong</a>
 */
public class RedissonCache<K, V> extends AbstractExternalCache<K, V> {
    private final RedissonClient client;
    private final RedissonCacheConfig<K, V> config;
    private final Function<Object, byte[]> valueEncoder;
    private final Function<byte[], Object> valueDecoder;

    public RedissonCache(final RedissonCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.client = config.getRedissonClient();
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();
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

    private Codec getCodec() {
        return ByteArrayCodec.INSTANCE;
    }

    private byte[] encoder(final CacheValueHolder<V> holder) {
        if (Objects.nonNull(holder)) {
            return valueEncoder.apply(holder);
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    private CacheValueHolder<V> decoder(final K key, final byte[] data, final int counter) {
        CacheValueHolder<V> holder = null;
        if (Objects.nonNull(data) && data.length > 0) {
            try {
                holder = (CacheValueHolder<V>) valueDecoder.apply(data);
            } catch (CacheEncodeException e) {
                holder = compatibleOldVal(key, data, counter + 1);
                if(Objects.isNull(holder)){
                    logError("decoder", key, e);
                }
            } catch (Throwable e) {
                logError("decoder", key, e);
            }
        }
        return holder;
    }

    private CacheValueHolder<V> decoder(final K key, final byte[] data) {
        return decoder(key, data, 0);
    }

    private CacheValueHolder<V> compatibleOldVal(final K key, final byte[] data, final int counter) {
        if (Objects.nonNull(key) && Objects.nonNull(data) && data.length > 0 && counter <= 1) {
            try {
                final Codec codec = this.client.getConfig().getCodec();
                if (Objects.nonNull(codec)) {
                    final Class<?> cls = ByteArrayCodec.class;
                    if (codec.getClass() != cls) {
                        final ByteBuf in = ByteBufAllocator.DEFAULT.buffer().writeBytes(data);
                        final byte[] out = (byte[]) codec.getValueDecoder().decode(in, null);
                        return decoder(key, out, counter);
                    }
                }
            } catch (Throwable e) {
                logError("compatibleOldVal", key, e);
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected CacheGetResult<V> do_GET(final K key) {
        try {
            final RBucket<byte[]> rb = this.client.getBucket(getCacheKey(key), getCodec());
            final CacheValueHolder<V> holder = decoder(key, rb.get());
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
                keys.stream().filter(Objects::nonNull).forEach(k -> {
                    final String key = getCacheKey(k);
                    if (Objects.nonNull(key)) {
                        keyMap.put(k, key);
                    }
                });
                if (!keyMap.isEmpty()) {
                    final Map<String, byte[]> kvMap = this.client.getBuckets(getCodec()).get(keyMap.values().toArray(new String[0]));
                    final long now = System.currentTimeMillis();
                    for (K k : keys) {
                        final String key = keyMap.get(k);
                        if (Objects.nonNull(key) && Objects.nonNull(kvMap)) {
                            final CacheValueHolder<V> holder = decoder(k, kvMap.get(key));
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
            this.client.getBucket(getCacheKey(key), getCodec()).set(encoder(holder), expireAfterWrite, timeUnit);
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
                    batch.getBucket(getCacheKey(k), getCodec()).setAsync(encoder(holder), expireAfterWrite, timeUnit);
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
            final boolean ret = this.client.getBucket(getCacheKey(key), getCodec()).delete();
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
                keys.forEach(key -> batch.getBucket(getCacheKey(key), getCodec()).deleteAsync());
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
            final Duration expire = Duration.ofMillis(timeUnit.toMillis(expireAfterWrite));
            final CacheValueHolder<V> holder = new CacheValueHolder<>(value, expire.toMillis());
            final boolean success = this.client.getBucket(getCacheKey(key), getCodec()).setIfAbsent(encoder(holder), expire);
            return success ? CacheResult.SUCCESS_WITHOUT_MSG : CacheResult.EXISTS_WITHOUT_MSG;
        } catch (Throwable e) {
            logError("PUT_IF_ABSENT", key, e);
            return new CacheResult(e);
        }
    }
}