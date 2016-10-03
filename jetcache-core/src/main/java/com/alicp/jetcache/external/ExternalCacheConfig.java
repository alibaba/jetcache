package com.alicp.jetcache.external;

import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.CacheConfig;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class ExternalCacheConfig extends CacheConfig {
    private String valueSerialPolicy;
    private String keyPrefix;

    public String getValueSerialPolicy() {
        return valueSerialPolicy;
    }

    public void setValueSerialPolicy(String valueSerialPolicy) {
        this.valueSerialPolicy = valueSerialPolicy;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
