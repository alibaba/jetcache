package com.alicp.jetcache.external;

import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.CacheBuilder;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class ExternalCacheBuilder<T extends ExternalCacheBuilder<T>> extends CacheBuilder<T> {

    public T valueSerialPolicy(SerialPolicy serialPolicy){
        ((ExternalCacheConfig)getConfig()).setValueSerialPolicy(serialPolicy);
        return self();
    }

    public T keyPrefix(String keyPrefix){
        ((ExternalCacheConfig)getConfig()).setKeyPrefix(keyPrefix);
        return self();
    }
}
