package com.alicp.jetcache.external;

import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.cache.CacheConfig;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class ExternalCacheConfig extends CacheConfig {
    private SerialPolicy valueSerialPolicy;
    private String keyPrefix;

    public SerialPolicy getValueSerialPolicy() {
        return valueSerialPolicy;
    }

    public void setValueSerialPolicy(SerialPolicy valueSerialPolicy) {
        this.valueSerialPolicy = valueSerialPolicy;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
