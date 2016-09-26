package com.alicp.jetcache.anno.impl.factory;

import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.embedded.LocalCacheConfig;

/**
 * Created on 2016/9/26.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LocalCacheFactory extends CacheFactory {

    @Override
    protected LocalCacheConfig getConfig() {
        if (config == null) {
            return new LocalCacheConfig();
        }
        return (LocalCacheConfig) config;
    }

    @Override
    public Cache buildCache(String subArea) {
        getConfig().setSubArea(subArea);
        return new LinkedHashMapCache(getConfig());
    }

    public void setLimit(int limit){
        getConfig().setLimit(limit);
    }

    public void setSoftRef(boolean useSoftRef){
        getConfig().setUseSoftRef(useSoftRef);
    }
}
