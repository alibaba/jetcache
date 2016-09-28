package com.alicp.jetcache.anno.impl.factory;

import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;

/**
 * Created on 2016/9/26.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LocalCacheFactory extends CacheFactory {

    @Override
    protected EmbeddedCacheConfig getConfig() {
        if (config == null) {
            return new EmbeddedCacheConfig();
        }
        return (EmbeddedCacheConfig) config;
    }

    @Override
    public Cache buildCache() {
        return new LinkedHashMapCache((EmbeddedCacheConfig) getConfig().clone());
    }

    public void setLimit(int limit){
        getConfig().setLimit(limit);
    }

    public void setSoftRef(boolean useSoftRef){
        getConfig().setUseSoftRef(useSoftRef);
    }
}
