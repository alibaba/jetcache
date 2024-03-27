package com.alicp.jetcache.redis;

import com.alicp.jetcache.external.ExternalCacheConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.util.Pool;

/**
 * Created on 2016/10/7.
 *
 * @author huangli
 */
public class RedisCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private Pool<Jedis> jedisPool;
    private Pool<Jedis>[] jedisSlavePools;
    private UnifiedJedis jedis;
    private UnifiedJedis[] slaves;
    private boolean readFromSlave;
    private int[] slaveReadWeights;

    public Pool<Jedis> getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }

    public Pool<Jedis>[] getJedisSlavePools() {
        return jedisSlavePools;
    }

    public void setJedisSlavePools(Pool<Jedis>... jedisSlavePools) {
        this.jedisSlavePools = jedisSlavePools;
    }

    public UnifiedJedis getJedis() {
        return jedis;
    }

    public void setJedis(UnifiedJedis jedis) {
        this.jedis = jedis;
    }

    public UnifiedJedis[] getSlaves() {
        return slaves;
    }

    public void setSlaves(UnifiedJedis[] slaves) {
        this.slaves = slaves;
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
