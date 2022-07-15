package com.alicp.jetcache.redisson;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.BroadcastManager;
import org.redisson.api.RedissonClient;

/**
 * Created on 2022/7/12.
 *
 * @author <a href="mailto:jeason1914@qq.com">yangyong</a>
 */
public class RedissonCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    public static class RedissonDataCacheBuilderImpl extends RedissonCacheBuilder<RedissonDataCacheBuilderImpl> {

    }

    public static RedissonDataCacheBuilderImpl createBuilder() {
        return new RedissonDataCacheBuilderImpl();
    }

    @SuppressWarnings({"all"})
    protected RedissonCacheBuilder() {
        buildFunc(config -> new RedissonCache((RedissonCacheConfig) config));
    }

    @Override
    @SuppressWarnings({"all"})
    public RedissonCacheConfig getConfig() {
        if (this.config == null) {
            this.config = new RedissonCacheConfig();
        }
        return (RedissonCacheConfig) this.config;
    }

    public T redissonClient(final RedissonClient client) {
        this.getConfig().setRedissonClient(client);
        return self();
    }

    @Override
    public boolean supportBroadcast() {
        return true;
    }

    @Override
    public BroadcastManager createBroadcastManager(final CacheManager cacheManager) {
        final RedissonCacheConfig<?, ?> c = (RedissonCacheConfig<?, ?>) this.getConfig().clone();
        return new RedissonBroadcastManager(cacheManager, c);
    }
}
