package com.alicp.jetcache.external;

import com.alicp.jetcache.SerialPolicy;
import com.alicp.jetcache.cache.CacheBuilderConfig;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExternalCacheConfig extends CacheBuilderConfig {
    private SerialPolicy valueSerialPolicy;

    public SerialPolicy getValueSerialPolicy() {
        return valueSerialPolicy;
    }

    public void setValueSerialPolicy(SerialPolicy valueSerialPolicy) {
        this.valueSerialPolicy = valueSerialPolicy;
    }
}
