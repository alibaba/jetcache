package com.alicp.jetcache.external;

import com.alicp.jetcache.SerialPolicy;
import com.alicp.jetcache.cache.CacheBuilderConfig;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExternalCacheConfig extends CacheBuilderConfig {
    private SerialPolicy serialPolicy;

    public SerialPolicy getSerialPolicy() {
        return serialPolicy;
    }

    public void setSerialPolicy(SerialPolicy serialPolicy) {
        this.serialPolicy = serialPolicy;
    }
}
