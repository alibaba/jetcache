package com.alicp.jetcache.redis.jedis;

import com.alicp.jetcache.external.ExternalCacheConfig;
import redis.clients.jedis.JedisCluster;

/**
 * Created on 2019/12/11.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public class JedisClusterCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private JedisCluster jedisCluster;

    private boolean enablePipeline = false;

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(final JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    public boolean isEnablePipeline() {
        return enablePipeline;
    }

    public void setEnablePipeline(boolean enablePipeline) {
        this.enablePipeline = enablePipeline;
    }
}
