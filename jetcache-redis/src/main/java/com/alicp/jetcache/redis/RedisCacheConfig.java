package com.alicp.jetcache.redis;

import com.alicp.jetcache.external.ExternalCacheConfig;
import redis.clients.jedis.JedisPool;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisCacheConfig extends ExternalCacheConfig {

    private JedisPool jedisPool;

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
