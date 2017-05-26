package com.alicp.jetcache.external;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractExternalCache<K, V> extends AbstractCache<K, V> {

    private ExternalCacheConfig<K, V> config;

    public AbstractExternalCache(ExternalCacheConfig<K, V> config) {
        this.config = config;
        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }
    }

    public byte[] buildKey(K key) {
        try {
            if (key instanceof byte[]) {
                return (byte[]) key;
            }
            Object newKey = key;
            if (config.getKeyConvertor() != null) {
                newKey = config.getKeyConvertor().apply(key);
            }
            return ExternalKeyUtil.buildKeyAfterConvert(newKey, config.getKeyPrefix());
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

}
