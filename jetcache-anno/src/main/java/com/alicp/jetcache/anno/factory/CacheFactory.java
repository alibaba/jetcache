package com.alicp.jetcache.anno.factory;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;

import java.util.function.Function;

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

    public void setDefaultExpireInMillis(int defaultExpireInMillis) {
        getConfig().setDefaultExpireInMillis(defaultExpireInMillis);
    }

    public void setKeyConvertor(Function<Object,Object> keyConvertor){
        getConfig().setKeyConvertor(keyConvertor);
    }

    public void setExpireAfterAccess(boolean expireAfterAccess){
        getConfig().setExpireAfterAccess(expireAfterAccess);
    }

}
