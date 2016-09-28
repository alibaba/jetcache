package com.alicp.jetcache.anno.impl.factory;

import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.cache.CacheBuilder;
import com.alicp.jetcache.cache.CacheConfig;
import com.alicp.jetcache.cache.KeyGenerator;

/**
 * Created on 2016/9/26.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class CacheFactory {

    protected CacheConfig config;

    public abstract Cache buildCache();

    protected CacheConfig getConfig() {
        if (config == null) {
            return new CacheConfig();
        }
        return config;
    }

    public void setCacheNullValue(boolean cacheNullValue) {
        getConfig().setCacheNullValue(cacheNullValue);
    }

    public void setDefaultTtlInSeconds(int defaultTtlInSeconds) {
        getConfig().setDefaultTtlInSeconds(defaultTtlInSeconds);
    }

    public void withKeyGenerator(KeyGenerator keyGenerator){
        getConfig().setKeyGenerator(keyGenerator);
    }

}
