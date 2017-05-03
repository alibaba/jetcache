package com.alicp.jetcache.redis.luttece;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.lambdaworks.redis.AbstractRedisClient;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisLutteceCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {
    public static class RedisLutteceCacheBuilderImpl extends RedisLutteceCacheBuilder<RedisLutteceCacheBuilderImpl> {
    }

    public static RedisLutteceCacheBuilderImpl createRedisCacheLutteceBuilder() {
        return new RedisLutteceCacheBuilderImpl();
    }

    public RedisLutteceCacheBuilder() {
//        buildFunc(config -> new RedisCache((RedisCacheLutteceConfig) config));
    }

    @Override
    protected RedisLutteceCacheConfig getConfig() {
        if (config == null) {
            config = new RedisLutteceCacheConfig();
        }
        return (RedisLutteceCacheConfig) config;
    }

    public T redisClient(AbstractRedisClient redisClient){
        getConfig().setRedisClient(redisClient);
        return self();
    }

    public void setRedisClient(AbstractRedisClient redisClient) {
        getConfig().setRedisClient(redisClient);
    }
}
