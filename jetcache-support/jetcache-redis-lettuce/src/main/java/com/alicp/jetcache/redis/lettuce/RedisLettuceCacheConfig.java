package com.alicp.jetcache.redis.lettuce;

import com.alicp.jetcache.external.ExternalCacheConfig;
import io.lettuce.core.AbstractRedisClient;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisLettuceCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private AbstractRedisClient redisClient;

    public AbstractRedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(AbstractRedisClient redisClient) {
        this.redisClient = redisClient;
    }
}
