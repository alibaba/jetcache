package com.alicp.jetcache.local;

import com.alicp.jetcache.cache.CacheBuilder;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LocalCacheBuilder<T extends LocalCacheBuilder<T>> extends CacheBuilder<T> {

    public LocalCacheBuilder(){
    }

    public static class LocalCacheBuilderImpl extends LocalCacheBuilder<LocalCacheBuilderImpl>{
    };

    public static LocalCacheBuilderImpl createLocalCacheBuilder(){
        return new LocalCacheBuilderImpl();
    }

    @Override
    protected LocalCacheConfig getConfig() {
        if (config == null) {
            return new LocalCacheConfig();
        }
        return (LocalCacheConfig) config;
    }

    public T withLimit(int limit){
        getConfig().setLimit(limit);
        return self();
    }

    public T withUseSoftRef(boolean useSoftRef){
        getConfig().setUseSoftRef(useSoftRef);
        return self();
    }
}
