package com.alicp.jetcache.factory;

import com.alicp.jetcache.embedded.EmbeddedCacheConfig;

/**
 * Created on 2016/9/26.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class EmbeddedCacheFactory extends CacheFactory {

    @Override
    protected EmbeddedCacheConfig getConfig() {
        if (config == null) {
            config = new EmbeddedCacheConfig();
        }
        return (EmbeddedCacheConfig) config;
    }

    public void setLimit(int limit){
        getConfig().setLimit(limit);
    }

    public void setSoftValues(boolean softValues){
        getConfig().setSoftValues(softValues);
    }

    public void setWeakValues(boolean weakValues){
        getConfig().setWeakValues(weakValues);
    }
}
