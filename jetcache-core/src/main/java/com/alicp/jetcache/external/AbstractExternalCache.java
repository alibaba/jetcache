package com.alicp.jetcache.external;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;

import java.io.*;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractExternalCache<K, V> extends AbstractCache<K, V> {

    private ExternalCacheConfig config;

    public AbstractExternalCache(ExternalCacheConfig config) {
        this.config = config;
        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }
    }

    protected byte[] buildKey(Object key) {
        try {
            if (config.getKeyConvertor() != null) {
                key = config.getKeyConvertor().apply(key);
            }
            return ExternalKeyUtil.buildKeyAfterConvert(key, config.getKeyPrefix());
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

}
