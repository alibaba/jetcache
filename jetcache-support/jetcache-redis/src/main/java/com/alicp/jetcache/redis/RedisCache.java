package com.alicp.jetcache.redis;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.redis.jedis.AbstractJedisPipeline;
import com.alicp.jetcache.redis.jedis.AbstractRedisJedisCache;
import com.alicp.jetcache.redis.jedis.JedisPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisCache<K, V> extends AbstractRedisJedisCache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private RedisCacheConfig<K, V> config;

    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public RedisCache(RedisCacheConfig<K, V> config) {
        super(config);
        this.config = config;

        if (config.getJedisPool() == null) {
            throw new CacheConfigException("no pool");
        }
        if (config.isReadFromSlave()) {
            if (config.getJedisSlavePools() == null || config.getJedisSlavePools().length == 0) {
                throw new CacheConfigException("slaves not config");
            }
            if (config.getSlaveReadWeights() == null) {
                initDefaultWeights(config);
            } else if (config.getSlaveReadWeights().length != config.getJedisSlavePools().length) {
                logger.error("length of slaveReadWeights and jedisSlavePools not equals, using default weights");
                initDefaultWeights(config);
            }
        }
        if (config.isExpireAfterAccess()) {
            throw new CacheConfigException("expireAfterAccess is not supported");
        }
    }

    private void initDefaultWeights(final RedisCacheConfig<K, V> config) {
        int len = config.getJedisSlavePools().length;
        int[] weights = new int[len];
        Arrays.fill(weights, 100);
        config.setSlaveReadWeights(weights);
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if (Pool.class.isAssignableFrom(clazz)) {
            return (T) config.getJedisPool();
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    protected byte[] jedisGet(final byte[] key) {
        try (Jedis jedis = getReadPool().getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    protected List<byte[]> jedisMget(final byte[]... keys) {
        try (Jedis jedis = getReadPool().getResource()) {
            return jedis.mget(keys);
        }
    }

    @Override
    protected String jedisPsetex(final byte[] key, final long milliseconds, final byte[] value) {
        try (Jedis jedis = config.getJedisPool().getResource()) {
            return jedis.psetex(key, milliseconds, value);
        }
    }

    @Override
    protected Long jedisDel(byte[]... keys) {
        try (Jedis jedis = config.getJedisPool().getResource()) {
            return jedis.del(keys);
        }
    }

    @Override
    protected String jedisSet(byte[] key, byte[] value, SetParams params) {
        try (Jedis jedis = config.getJedisPool().getResource()) {
            return jedis.set(key, value, params);
        }
    }

    @Override
    protected AbstractJedisPipeline getJedisPipeline() {
        return new JedisPipeline(config.getJedisPool().getResource());
    }

    @Override
    protected boolean isEnablePipeline() {
        return true;
    }

    protected Pool<Jedis> getReadPool() {
        if (!config.isReadFromSlave()) {
            return config.getJedisPool();
        }
        int[] weights = config.getSlaveReadWeights();
        int index = randomIndex(weights);
        return config.getJedisSlavePools()[index];
    }

    protected static int randomIndex(final int[] weights) {
        int sumOfWeights = 0;
        for (int w : weights) {
            sumOfWeights += w;
        }
        int r = random.nextInt(sumOfWeights);
        int x = 0;
        for (int i = 0; i < weights.length; i++) {
            x += weights[i];
            if (r < x) {
                return i;
            }
        }
        throw new CacheException("assert false");
    }
}
