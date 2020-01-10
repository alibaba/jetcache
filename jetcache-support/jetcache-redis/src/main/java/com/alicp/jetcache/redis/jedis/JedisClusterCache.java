package com.alicp.jetcache.redis.jedis;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

import java.util.List;

/**
 * Created on 2019/12/11.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public class JedisClusterCache<K, V> extends AbstractRedisJedisCache<K, V> {

    private JedisClusterCacheConfig config;

    public JedisClusterCache(final JedisClusterCacheConfig<K, V> config) {
        super(config);
        this.config = config;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if (JedisCluster.class.isAssignableFrom(clazz)) {
            return (T) config.getJedisCluster();
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    protected byte[] jedisGet(final byte[] key) {
        return config.getJedisCluster().get(key);
    }

    @Override
    protected List<byte[]> jedisMget(final byte[]... keys) {
        return config.getJedisCluster().mget(keys);
    }

    @Override
    protected String jedisPsetex(final byte[] key, final long milliseconds, final byte[] value) {
        return config.getJedisCluster().psetex(key, milliseconds, value);
    }

    @Override
    protected Long jedisDel(final byte[]... keys) {
        return config.getJedisCluster().del(keys);
    }

    @Override
    protected String jedisSet(final byte[] key, final byte[] value, final SetParams params) {
        return config.getJedisCluster().set(key, value, params);
    }

    @Override
    protected AbstractJedisPipeline getJedisPipeline() {
        return new JedisClusterPipeline(config.getJedisCluster());
    }

    @Override
    protected boolean isEnablePipeline() {
        return config.isEnablePipeline();
    }
}
