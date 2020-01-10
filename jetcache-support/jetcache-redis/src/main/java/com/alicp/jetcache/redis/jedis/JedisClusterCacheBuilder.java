package com.alicp.jetcache.redis.jedis;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import redis.clients.jedis.JedisCluster;

/**
 * Created on 2019/12/11.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public class JedisClusterCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    public static class JedisClusterCacheBuilderImpl extends JedisClusterCacheBuilder<JedisClusterCacheBuilderImpl> {
    }

    public static JedisClusterCacheBuilderImpl createJedisClusterCacheBuilder() {
        return new JedisClusterCacheBuilderImpl();
    }

    protected JedisClusterCacheBuilder() {
        buildFunc(config -> new JedisClusterCache((JedisClusterCacheConfig) config));
    }

    @Override
    public JedisClusterCacheConfig getConfig() {
        if (config == null) {
            config = new JedisClusterCacheConfig();
        }
        return (JedisClusterCacheConfig) config;
    }

    public T jedisCluster(final JedisCluster jedisCluster) {
        getConfig().setJedisCluster(jedisCluster);
        return self();
    }

    public T enablePipeline(final boolean enablePipeline) {
        getConfig().setEnablePipeline(enablePipeline);
        return self();
    }
}
