package com.alicp.jetcache.redis;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {
    public static class RedisCacheBuilderImpl extends RedisCacheBuilder<RedisCacheBuilderImpl> {
    }

    public static RedisCacheBuilderImpl createRedisCacheBuilder() {
        return new RedisCacheBuilderImpl();
    }

    protected RedisCacheBuilder() {
        buildFunc(config -> new RedisCache((RedisCacheConfig) config));
    }

    @Override
    public RedisCacheConfig getConfig() {
        if (config == null) {
            config = new RedisCacheConfig();
        }
        return (RedisCacheConfig) config;
    }

    public T jedisPool(Pool<Jedis> pool) {
        getConfig().setJedisPool(pool);
        return self();
    }

    public void setJedisPool(Pool<Jedis> jedisPool) {
        getConfig().setJedisPool(jedisPool);
    }

    public T readFromSlave(boolean readFromSlave) {
        getConfig().setReadFromSlave(readFromSlave);
        return self();
    }

    public void setReadFromSlave(boolean readFromSlave) {
        getConfig().setReadFromSlave(readFromSlave);
    }

    public T jedisSlavePools(Pool<Jedis>... jedisSlavePools) {
        getConfig().setJedisSlavePools(jedisSlavePools);
        return self();
    }

    public void setJedisSlavePools(Pool<Jedis>... jedisSlavePools) {
        getConfig().setJedisSlavePools(jedisSlavePools);
    }

    public T slaveReadWeights(int... slaveReadWeights) {
        getConfig().setSlaveReadWeights(slaveReadWeights);
        return self();
    }

    public void setSlaveReadWeights(int... slaveReadWeights) {
        getConfig().setSlaveReadWeights(slaveReadWeights);
    }

}
