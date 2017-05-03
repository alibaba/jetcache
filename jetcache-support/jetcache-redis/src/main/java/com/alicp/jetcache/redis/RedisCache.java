package com.alicp.jetcache.redis;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import java.util.*;
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
    private Pool<Jedis> pool;

    public RedisCache(RedisCacheConfig config) {
        super(config);
        this.config = config;
        this.pool = config.getJedisPool();
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();

        if (pool == null) {
            throw new CacheConfigException("no pool");
        }
        if (config.isExpireAfterAccess()) {
            throw new CacheConfigException("expireAfterAccess is not supported");
        }
    }

    @Override
    public CacheConfig config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(JedisPool.class)) {
            return (T) pool;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    public CacheGetResult<V> GET(K key) {
        if (key == null) {
            return new CacheGetResult<V>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        try (Jedis jedis = pool.getResource()) {
            byte[] newKey = buildKey(key);
            byte[] bytes = jedis.get(newKey);
            if (bytes != null) {
                CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(bytes);
                if (System.currentTimeMillis() >= holder.getExpireTime()) {
                    return CacheGetResult.EXPIRED_WITHOUT_MSG;
                }
                return new CacheGetResult(CacheResultCode.SUCCESS, null, holder.getValue());
            } else {
                return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
            }
        } catch (Exception ex) {
            logError("GET", key, ex);
            return new CacheGetResult(ex);
        }
    }

    @Override
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        try (Jedis jedis = pool.getResource()) {
            ArrayList<K> keyList = new ArrayList<K>(keys);
            byte[][] newKeys = keyList.stream().map((k) -> buildKey(k)).toArray(byte[][]::new);

            Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
            if (newKeys.length > 0) {
                List mgetResults = jedis.mget(newKeys);
                for (int i = 0; i < mgetResults.size(); i++) {
                    Object value = mgetResults.get(i);
                    K key = keyList.get(i);
                    if (value != null) {
                        CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply((byte[]) value);
                        if (System.currentTimeMillis() >= holder.getExpireTime()) {
                            resultMap.put(key, CacheGetResult.EXPIRED_WITHOUT_MSG);
                        } else {
                            CacheGetResult<V> r = new CacheGetResult<V>(CacheResultCode.SUCCESS, null, holder.getValue());
                            resultMap.put(key, r);
                        }
                    } else {
                        resultMap.put(key, CacheGetResult.NOT_EXISTS_WITHOUT_MSG);
                    }
                }
            }
            return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, resultMap);
        } catch (Exception ex) {
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<K, V>(ex);
        }
    }


    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (Jedis jedis = pool.getResource()) {
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
            return new CacheResult(ex);
        }
    }

    @Override
    public CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expire, TimeUnit timeUnit) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (Jedis jedis = pool.getResource()) {
            int failCount = 0;
            List<Response<String>> responses = new ArrayList<>();
            Pipeline p = jedis.pipelined();
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), System.currentTimeMillis(), timeUnit.toMillis(expire));
                Response<String> resp = p.psetex(buildKey(en.getKey()), timeUnit.toMillis(expire), valueEncoder.apply(holder));
                responses.add(resp);
            }
            p.sync();
            for (Response<String> resp : responses) {
                if(!"OK".equals(resp.get())){
                    failCount++;
                }
            }
            return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                    failCount == map.size() ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    public CacheResult REMOVE(K key) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        return REMOVE_impl(key, buildKey(key));
    }

    private CacheResult REMOVE_impl(Object key, byte[] newKey) {
        try (Jedis jedis = pool.getResource()) {
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
            return new CacheResult(ex);
        }
    }

    @Override
    public CacheResult REMOVE_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (Jedis jedis = pool.getResource()) {
            byte[][] newKeys = keys.stream().map((k) -> buildKey(k)).toArray((len) -> new byte[keys.size()][]);
            jedis.del(newKeys);
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        if (key == null) {
            return null;
        }
        try (Jedis jedis = pool.getResource()) {
            final String uuid = UUID.randomUUID().toString();
            final byte[] newKey = buildKey(key);
            final long expireTimestamp = System.currentTimeMillis() + timeUnit.toMillis(expire);

            AutoReleaseLock lock = () -> {
                if (System.currentTimeMillis() < expireTimestamp) {
                    CacheResult cacheResult = REMOVE_impl(key, newKey);
                    if (cacheResult.getResultCode() == CacheResultCode.FAIL && System.currentTimeMillis() < expireTimestamp) {
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
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (Jedis jedis = pool.getResource()) {
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
            return new CacheResult(ex);
        }
    }

    @Override
    protected boolean needLogStackTrace(Throwable e) {
        if (e instanceof JedisConnectionException) {
            return false;
        }
        return true;
    }
}
