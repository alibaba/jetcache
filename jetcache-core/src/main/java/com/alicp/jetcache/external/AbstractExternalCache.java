package com.alicp.jetcache.external;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfigException;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractExternalCache<K, V> implements Cache<K, V> {

    public AbstractExternalCache(ExternalCacheConfig config){
        if (config.getKeyConvertor() == null) {
            throw new CacheConfigException("no key generator");
        }
        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }
    }

}
