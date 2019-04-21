package com.alicp.jetcache.redis.springdata;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Created on 2019/4/21.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringDataRedisCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {
    public static class SpringDataRedisCacheBuilderImpl extends SpringDataRedisCacheBuilder<SpringDataRedisCacheBuilderImpl> {
    }

    public static SpringDataRedisCacheBuilderImpl createBuilder() {
        return new SpringDataRedisCacheBuilderImpl();
    }

    protected SpringDataRedisCacheBuilder() {
        buildFunc(config -> new SpringDataRedisCache((SpringDataRedisCacheConfig) config));
    }

    @Override
    public SpringDataRedisCacheConfig getConfig() {
        if (config == null) {
            config = new SpringDataRedisCacheConfig();
        }
        return (SpringDataRedisCacheConfig) config;
    }

    public T connectionFactory(RedisConnectionFactory connectionFactory) {
        getConfig().setConnectionFactory(connectionFactory);
        return self();
    }

    public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
        getConfig().setConnectionFactory(connectionFactory);
    }

}
