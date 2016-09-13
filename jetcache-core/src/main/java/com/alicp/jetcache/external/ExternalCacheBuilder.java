package com.alicp.jetcache.external;

import com.alicp.jetcache.SerialPolicy;
import com.alicp.jetcache.cache.CacheBuilder;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExternalCacheBuilder<T extends ExternalCacheBuilder<T>> extends CacheBuilder<T> {

    public static class ExternalCacheBuilderImpl extends ExternalCacheBuilder<ExternalCacheBuilderImpl>{
    };

    public static ExternalCacheBuilderImpl createLocalCacheBuilder(){
        return new ExternalCacheBuilderImpl();
    }

    @Override
    protected ExternalCacheConfig getConfig() {
        if (config == null) {
            return new ExternalCacheConfig();
        }
        return (ExternalCacheConfig) config;
    }

    public T withSerialPolicy(SerialPolicy serialPolicy){
        getConfig().setSerialPolicy(serialPolicy);
        return self();
    }

}
