package com.alicp.jetcache.anno.impl.factory;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.KeyGenerator;

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

    public void setDefaultExpireInMillis(int defaultExpireInMillis) {
        getConfig().setDefaultExpireInMillis(defaultExpireInMillis);
    }

    public void withKeyGenerator(KeyGenerator keyGenerator){
        getConfig().setKeyGenerator(keyGenerator);
    }

}
