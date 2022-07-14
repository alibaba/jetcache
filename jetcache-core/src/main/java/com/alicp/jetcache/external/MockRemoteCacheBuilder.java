package com.alicp.jetcache.external;

import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;

import java.util.function.Consumer;

/**
 * Created on 2016/10/20.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MockRemoteCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    public static class MockRemoteCacheBuilderImpl extends MockRemoteCacheBuilder<MockRemoteCacheBuilderImpl> {
    }

    public static MockRemoteCacheBuilderImpl createMockRemoteCacheBuilder(){
        return new MockRemoteCacheBuilderImpl();
    }

    @Override
    public MockRemoteCacheConfig getConfig() {
        if (config == null) {
            config = new MockRemoteCacheConfig();
        }
        return (MockRemoteCacheConfig) config;
    }

    @Override
    public boolean supportBroadcast() {
        return true;
    }

    @Override
    public BroadcastManager createBroadcastManager() {
        return new BroadcastManager() {
            @Override
            public CacheResult publish(CacheMessage cacheMessage) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            }

            @Override
            public void startSubscribe(Consumer<CacheMessage> consumer) {
            }
        };
    }

    public MockRemoteCacheBuilder() {
        this.setKeyPrefix("DEFAULT_PREFIX");
        buildFunc((c) -> new MockRemoteCache((MockRemoteCacheConfig) c));
    }

    public T limit(int limit){
        getConfig().setLimit(limit);
        return self();
    }

    public void setLimit(int limit){
        getConfig().setLimit(limit);
    }
}
