package com.alicp.jetcache.embedded;

import com.alicp.jetcache.CacheBuilder;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class EmbeddedCacheBuilder<T extends EmbeddedCacheBuilder<T>> extends CacheBuilder<T> {

    public EmbeddedCacheBuilder(){
    }

    public static class EmbeddedCacheBuilderImpl extends EmbeddedCacheBuilder<EmbeddedCacheBuilderImpl> {
    };

    public static EmbeddedCacheBuilderImpl createEmbeddedCacheBuilder(){
        return new EmbeddedCacheBuilderImpl();
    }

    @Override
    protected EmbeddedCacheConfig getConfig() {
        if (config == null) {
            return new EmbeddedCacheConfig();
        }
        return (EmbeddedCacheConfig) config;
    }

    public T limit(int limit){
        getConfig().setLimit(limit);
        return self();
    }

    public T softValues(boolean useSoftRef){
        getConfig().setSoftValues(useSoftRef);
        return self();
    }
}
