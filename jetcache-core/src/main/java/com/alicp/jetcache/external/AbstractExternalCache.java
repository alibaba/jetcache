package com.alicp.jetcache.external;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.RefreshCache;
import com.alicp.jetcache.anno.KeyConvertor;

import java.io.IOException;

/**
 * Created on 2016/10/8.
 *
 * @author huangli
 */
public abstract class AbstractExternalCache<K, V> extends AbstractCache<K, V> {

    private ExternalCacheConfig<K, V> config;

    public AbstractExternalCache(ExternalCacheConfig<K, V> config) {
        this.config = config;
        checkConfig();
    }

    protected void checkConfig() {
        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }
        if (config.getKeyPrefix() == null) {
            throw new CacheConfigException("keyPrefix is required");
        }
    }

    public byte[] buildKey(K key) {
        try {
            Object newKey = key;
            if (config.getKeyConvertor() != null) {
                if (config.getKeyConvertor() instanceof KeyConvertor) {
                    if (!isPreservedKey(key)) {
                        // since 2.7.3 KeyConvertor extends Function<Object, Object>
                        newKey = config.getKeyConvertor().apply(key);
                    }
                } else {
                    // before 2.7.3, KeyConvertor is interface only place some constants.
                    // "key convertor" is Function<Object, Object> and can't process byte[] and String
                    if (key instanceof byte[]) {
                        newKey = key;
                    } else if (key instanceof String) {
                        newKey = key;
                    } else {
                        newKey = config.getKeyConvertor().apply(key);
                    }
                }
            }
            return ExternalKeyUtil.buildKeyAfterConvert(newKey, config.getKeyPrefix());
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

    private boolean isPreservedKey(Object key) {
        if (key instanceof byte[]) {
            byte[] keyBytes = (byte[]) key;
            return endWith(keyBytes, RefreshCache.LOCK_KEY_SUFFIX)
                    || endWith(keyBytes, RefreshCache.TIMESTAMP_KEY_SUFFIX);
        }
        return false;
    }

    private boolean endWith(byte[] key, byte[] suffix) {
        int len = suffix.length;
        if (key.length < len) {
            return false;
        }
        int startPos = key.length - len;
        for (int i = 0; i < len; i++) {
            if (key[startPos + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }

}
