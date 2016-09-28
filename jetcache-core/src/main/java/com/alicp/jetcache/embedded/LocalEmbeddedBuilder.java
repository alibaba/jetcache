package com.alicp.jetcache.embedded;

import com.alicp.jetcache.cache.CacheBuilder;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LocalEmbeddedBuilder<T extends LocalEmbeddedBuilder<T>> extends CacheBuilder<T> {

    public LocalEmbeddedBuilder(){
    }

    public static class LocalEmbeddedBuilderImpl extends LocalEmbeddedBuilder<LocalEmbeddedBuilderImpl> {
    };

    public static LocalEmbeddedBuilderImpl createLocalCacheBuilder(){
        return new LocalEmbeddedBuilderImpl();
    }

    @Override
    protected LocalEmbeddedConfig getConfig() {
        if (config == null) {
            return new LocalEmbeddedConfig();
        }
        return (LocalEmbeddedConfig) config;
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
