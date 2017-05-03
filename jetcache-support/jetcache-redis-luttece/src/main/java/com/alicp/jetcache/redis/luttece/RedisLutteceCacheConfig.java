package com.alicp.jetcache.redis.luttece;

import com.alicp.jetcache.external.ExternalCacheConfig;
import com.lambdaworks.redis.AbstractRedisClient;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisLutteceCacheConfig extends ExternalCacheConfig {

    private AbstractRedisClient redisClient;

    public AbstractRedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(AbstractRedisClient redisClient) {
        this.redisClient = redisClient;
    }
}
