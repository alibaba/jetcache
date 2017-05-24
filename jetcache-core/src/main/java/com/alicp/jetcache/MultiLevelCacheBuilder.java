package com.alicp.jetcache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017/5/24.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCacheBuilder<T extends MultiLevelCacheBuilder<T>> extends AbstractCacheBuilder<T> {
    public static class MultiLevelCacheBuilderImpl extends MultiLevelCacheBuilder<MultiLevelCacheBuilderImpl> {
    }

    public static MultiLevelCacheBuilderImpl createMultiLevelCacheBuilder() {
        return new MultiLevelCacheBuilderImpl();
    }

    protected MultiLevelCacheBuilder() {
        buildFunc(config -> new MultiLevelCache((MultiLevelCacheConfig) config));
    }

    @Override
    public MultiLevelCacheConfig getConfig() {
        if (config == null) {
            config = new MultiLevelCacheConfig();
        }
        return (MultiLevelCacheConfig) config;
    }

    public T addCache(Cache... caches) {
        for (Cache c : caches) {
            getConfig().getCaches().add(c);
        }
        return self();
    }

    public void setCaches(List<Cache> caches) {
        getConfig().setCaches(caches);
    }

}