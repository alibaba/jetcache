package com.alicp.jetcache.redis.springdata;

import com.alicp.jetcache.external.ExternalCacheConfig;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created on 2019/4/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisSpringDataCacheConfig<K, V> extends ExternalCacheConfig<K, V> {

    private RedisConnectionFactory connectionFactory;

    public RedisConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
