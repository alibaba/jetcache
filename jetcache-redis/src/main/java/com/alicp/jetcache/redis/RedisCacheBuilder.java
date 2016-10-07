package com.alicp.jetcache.redis;

import com.alicp.jetcache.external.ExternalCacheBuilder;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {
    public static class RedisCacheBuilderImpl extends RedisCacheBuilder<RedisCacheBuilderImpl> {
    }

    public static RedisCacheBuilderImpl createRedisCacheBuilder() {
        return new RedisCacheBuilderImpl();
    }

    public RedisCacheBuilder() {
        buildFunc(config -> new RedisCache((RedisCacheConfig) config));
    }

    @Override
    protected RedisCacheConfig getConfig() {
        if (config == null) {
            config = new RedisCacheConfig();
        }
        return (RedisCacheConfig) config;
    }

}
