package com.alicp.jetcache.redis;

import com.alicp.jetcache.external.ExternalCacheConfig;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private Pool jedisPool;
    private Pool[] jedisSlavePools;
    private boolean readFromSlave;
    private int[] slaveReadWeights;

    public Pool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(Pool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public Pool[] getJedisSlavePools() {
        return jedisSlavePools;
    }

    public void setJedisSlavePools(Pool... jedisSlavePools) {
        this.jedisSlavePools = jedisSlavePools;
    }

    public boolean isReadFromSlave() {
        return readFromSlave;
    }

    public void setReadFromSlave(boolean readFromSlave) {
        this.readFromSlave = readFromSlave;
    }

    public int[] getSlaveReadWeights() {
        return slaveReadWeights;
    }

    public void setSlaveReadWeights(int... slaveReadWeights) {
        this.slaveReadWeights = slaveReadWeights;
    }
}
