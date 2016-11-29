package com.alicp.jetcache.embedded;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCacheBuilder extends EmbeddedCacheBuilder {
    public static class LinkedHashMapCacheBuilderImpl extends EmbeddedCacheBuilder<LinkedHashMapCacheBuilderImpl> {
    }

    public static LinkedHashMapCacheBuilderImpl createLinkedHashMapCacheBuilder() {
        return new LinkedHashMapCacheBuilderImpl();
    }

    public LinkedHashMapCacheBuilder() {
        buildFunc((c) -> new LinkedHashMapCache((EmbeddedCacheConfig) c));
    }
}
