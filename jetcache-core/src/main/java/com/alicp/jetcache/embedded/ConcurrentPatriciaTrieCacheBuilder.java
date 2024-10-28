package com.alicp.jetcache.embedded;

/**
 * Created on 2016/11/29.
 *
 * @author huangli
 */
public class ConcurrentPatriciaTrieCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends EmbeddedCacheBuilder<T> {
    public static class ConcurrentPatriciaTrieCacheBuilderImpl extends ConcurrentPatriciaTrieCacheBuilder<ConcurrentPatriciaTrieCacheBuilderImpl> {
    }

    public static ConcurrentPatriciaTrieCacheBuilderImpl createConcurrentPatriciaTrieCacheBuilder() {
        return new ConcurrentPatriciaTrieCacheBuilderImpl();
    }

    protected ConcurrentPatriciaTrieCacheBuilder() {
        buildFunc((c) -> new ConcurrentPatriciaTrieCache((EmbeddedCacheConfig) c));
    }
}
