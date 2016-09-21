package com.alicp.jetcache.external;

import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.cache.CacheConfig;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExternalCacheConfig extends CacheConfig {
    private SerialPolicy valueSerialPolicy;

    public SerialPolicy getValueSerialPolicy() {
        return valueSerialPolicy;
    }

    public void setValueSerialPolicy(SerialPolicy valueSerialPolicy) {
        this.valueSerialPolicy = valueSerialPolicy;
    }
}
