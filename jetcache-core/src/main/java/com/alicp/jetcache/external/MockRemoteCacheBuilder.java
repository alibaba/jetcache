package com.alicp.jetcache.external;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;

/**
 * Created on 2016/10/20.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MockRemoteCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    private static boolean subscribeStart;
    private static CacheMessage lastPublishMessage;

    public static class MockRemoteCacheBuilderImpl extends MockRemoteCacheBuilder<MockRemoteCacheBuilderImpl> {
    }

    public static MockRemoteCacheBuilderImpl createMockRemoteCacheBuilder() {
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
    public BroadcastManager createBroadcastManager(CacheManager cacheManager) {
        return new BroadcastManager(cacheManager) {
            @Override
            public CacheResult publish(CacheMessage cacheMessage) {
                lastPublishMessage = cacheMessage;
                return CacheResult.SUCCESS_WITHOUT_MSG;
            }

            @Override
            public void startSubscribe() {
                subscribeStart = true;
            }
        };
    }

    public MockRemoteCacheBuilder() {
        this.setKeyPrefix("DEFAULT_PREFIX");
        buildFunc((c) -> new MockRemoteCache((MockRemoteCacheConfig) c));
    }

    public T limit(int limit) {
        getConfig().setLimit(limit);
        return self();
    }

    public void setLimit(int limit) {
        getConfig().setLimit(limit);
    }

    public static boolean isSubscribeStart() {
        return subscribeStart;
    }

    public static CacheMessage getLastPublishMessage() {
        return lastPublishMessage;
    }

    public static void reset() {
        subscribeStart = false;
        lastPublishMessage = null;
    }
}
