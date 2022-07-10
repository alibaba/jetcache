package com.alicp.jetcache.redis.lettuce;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.external.ExternalCacheConfig;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisLettuceCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private AbstractRedisClient redisClient;

    private StatefulConnection<byte[], byte[]> connection;

    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    private long asyncResultTimeoutInMillis = CacheConsts.ASYNC_RESULT_TIMEOUT.toMillis();

    public AbstractRedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(AbstractRedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public StatefulConnection<byte[], byte[]> getConnection() {
        return connection;
    }

    public void setConnection(StatefulConnection<byte[], byte[]> connection) {
        this.connection = connection;
    }

    public long getAsyncResultTimeoutInMillis() {
        return asyncResultTimeoutInMillis;
    }

    public void setAsyncResultTimeoutInMillis(long asyncResultTimeoutInMillis) {
        this.asyncResultTimeoutInMillis = asyncResultTimeoutInMillis;
    }

    public StatefulRedisPubSubConnection<byte[], byte[]> getPubSubConnection() {
        return pubSubConnection;
    }

    public void setPubSubConnection(StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }
}
