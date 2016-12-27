package com.alicp.jetcache.redis;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisCache<K, V> extends AbstractExternalCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private RedisCacheConfig config;

    Function<Object, byte[]> valueEncoder;
    Function<byte[], Object> valueDecoder;
    private JedisPool jedisPool;

    public RedisCache(RedisCacheConfig config) {
        super(config);
        this.config = config;
        this.jedisPool = config.getJedisPool();
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();

        if (jedisPool == null) {
            throw new CacheConfigException("no jedisPool");
        }
    }

    @Override
    public CacheConfig config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(JedisPool.class)) {
            return (T) jedisPool;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    protected CacheGetResult<CacheValueHolder<V>> getHolder(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] newKey = buildKey(key);

            if (!config.isExpireAfterAccess()) {
                byte[] bytes = jedis.get(newKey);
                if (bytes != null) {
                    CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(bytes);
                    if (System.currentTimeMillis() >= holder.getExpireTime()) {
                        return CacheGetResult.EXPIRED_WITHOUT_MSG;
                    }
                    return new CacheGetResult(CacheResultCode.SUCCESS, null, holder);
                } else {
                    return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
                }
            } else {
                Pipeline p = jedis.pipelined();
                Response<byte[]> valueResp = p.get(newKey);
                Response<Long> pttlResp = p.pttl(newKey);
                p.sync();
                if (valueResp.get() != null) {
                    CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(valueResp.get());
                    Long restTtl = pttlResp.get();
                    if (restTtl != null) {
                        holder.setExpireTime(System.currentTimeMillis() + restTtl);
                    }
                    jedis.pexpire(newKey, holder.getInitTtlInMillis());
                    return new CacheGetResult(CacheResultCode.SUCCESS, null, holder);
                } else {
                    return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
                }
            }
        } catch (Exception ex) {
            logError("GET", key, ex);
            return new CacheGetResult(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage(), null);
        }
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        try (Jedis jedis = jedisPool.getResource()) {
            CacheValueHolder<V> holder = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
            byte[] newKey = buildKey(key);
            String rt = jedis.psetex(newKey, timeUnit.toMillis(expire), valueEncoder.apply(holder));
            if ("OK".equals(rt)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, rt);
            }
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage());
        }
    }

    @Override
    public CacheResult REMOVE(K key) {
        return REMOVE_impl(key, buildKey(key));
    }

    private CacheResult REMOVE_impl(Object key, byte[] newKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            Long rt = jedis.del(newKey);
            if (rt == null) {
                return CacheResult.FAIL_WITHOUT_MSG;
            } else if (rt == 1) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else if (rt == 0) {
                return new CacheResult(CacheResultCode.NOT_EXISTS, null);
            } else {
                return CacheResult.FAIL_WITHOUT_MSG;
            }
        } catch (Exception ex) {
            logError("REMOVE", key, ex);
            return new CacheResult(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage());
        }
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        try (Jedis jedis = jedisPool.getResource()) {
            final String uuid = UUID.randomUUID().toString();
            final byte[] newKey = buildKey(key);
            final long expireTimestamp = System.currentTimeMillis() + timeUnit.toMillis(expire);

            AutoReleaseLock lock = () -> {
                if (System.currentTimeMillis() < expireTimestamp) {
                    CacheResult cacheResult = REMOVE_impl(key, newKey);
                    if (cacheResult.getResultCode() == CacheResultCode.FAIL) {
                        logger.warn("unlock key {} + failed, retry. msg = {}", key, cacheResult.getMessage());
                        cacheResult = REMOVE_impl(key, newKey);
                        if (cacheResult.getResultCode() == CacheResultCode.FAIL) {
                            logger.error("retry unlock key {} + failed. msg = {}", key, cacheResult.getMessage());
                        } else {
                            logger.debug("release lock: {}, {}", key, uuid);
                        }
                    } else {
                        logger.debug("release lock: {}, {}", key, uuid);
                    }
                } else {
                    logger.debug("lock expired: {}, {}", key, uuid);
                }
            };

            try {
                String rt = jedis.set(newKey, uuid.getBytes(), "NX".getBytes(), "PX".getBytes(), timeUnit.toMillis(expire));
                if ("OK".equals(rt)) {
                    logger.debug("get lock {},{}", key, uuid);
                    return lock;
                } else {
                    return null;
                }
            } catch (Exception e) {
                logger.warn("tryLock {} + failed, try get again. uuid={}, exClass={}, exMessage={}", key, uuid, e.getClass(), e.getMessage());

                try {
                    byte[] bs = jedis.get(newKey);
                    if (bs != null && uuid.equals(new String(bs))) {
                        logger.info("successful get lock {} after put failure, uuid={}", key, uuid);
                        return lock;
                    } else {
                        return null;
                    }
                } catch (Exception e2) {
                    logger.warn("tryLock(retry) {} + failed. uuid={}, exClass={}, exMessage={}", key, uuid, e2.getClass(), e2.getMessage());
                    return null;
                }
            }
        } catch (Exception ex) {
            logError("tryLock", key, ex);
            return null;
        }
    }

    @Override
    public CacheResult PUT_IF_ABSENT(K key, V value, long expire, TimeUnit timeUnit) {
        try (Jedis jedis = jedisPool.getResource()) {
            CacheValueHolder<V> holder = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
            byte[] newKey = buildKey(key);
            String rt = jedis.set(newKey, valueEncoder.apply(holder), "NX".getBytes(), "PX".getBytes(), timeUnit.toMillis(expire));
            if ("OK".equals(rt)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else if (rt == null) {
                return CacheResult.EXISTS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, rt);
            }
        } catch (Exception ex) {
            logError("PUT_IF_ABSENT", key, ex);
            return new CacheResult(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage());
        }
    }
}
