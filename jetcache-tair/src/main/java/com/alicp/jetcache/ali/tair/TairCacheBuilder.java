package com.alicp.jetcache.ali.tair;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.taobao.tair.TairManager;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TairCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {
    public static class TairCacheBuilderImpl extends TairCacheBuilder<TairCacheBuilderImpl> {
    }

    public static TairCacheBuilderImpl createTairCacheBuilder() {
        return new TairCacheBuilderImpl();
    }

    public TairCacheBuilder() {
        buildFunc(config -> new TairCache((TairCacheConfig) config));
    }

    @Override
    protected TairCacheConfig getConfig() {
        if (config == null) {
            config = new TairCacheConfig();
        }
        return (TairCacheConfig) config;
    }

    public T tairManager(TairManager tairManager) {
        getConfig().setTairManager(tairManager);
        return self();
    }

    public T namespace(int namespace) {
        getConfig().setNamespace(namespace);
        return self();
    }

    public void setNamespace(int namespace) {
        getConfig().setNamespace(namespace);
    }

    public void setTairManager(TairManager tairManager) {
        getConfig().setTairManager(tairManager);
    }
}
