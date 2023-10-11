package com.alicp.jetcache.embedded;

/**
 * Created on 2016/11/29.
 *
 * @author huangli
 */
public class LinkedHashMapCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends EmbeddedCacheBuilder<T> {
    public static class LinkedHashMapCacheBuilderImpl extends LinkedHashMapCacheBuilder<LinkedHashMapCacheBuilderImpl> {
    }

    public static LinkedHashMapCacheBuilderImpl createLinkedHashMapCacheBuilder() {
        return new LinkedHashMapCacheBuilderImpl();
    }

    protected LinkedHashMapCacheBuilder() {
        buildFunc((c) -> new LinkedHashMapCache((EmbeddedCacheConfig) c));
    }
}
