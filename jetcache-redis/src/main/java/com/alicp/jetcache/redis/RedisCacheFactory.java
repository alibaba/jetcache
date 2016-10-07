package com.alicp.jetcache.redis;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.factory.ExternalCacheFactory;
import redis.clients.jedis.JedisPool;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisCacheFactory extends ExternalCacheFactory {

    @Override
    protected RedisCacheConfig getConfig() {
        if (config == null) {
            config = new RedisCacheConfig();
        }
        return (RedisCacheConfig) config;
    }


    @Override
    public Cache buildCache() {
        return new RedisCache(getConfig());
    }

    public void setJedisPool(JedisPool jedisPool) {
        getConfig().setJedisPool(jedisPool);
    }
}
