package com.alicp.jetcache.external;

import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.CacheBuilder;

import java.util.function.Function;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class ExternalCacheBuilder<T extends ExternalCacheBuilder<T>> extends CacheBuilder<T> {

    public T keyPrefix(String keyPrefix){
        ((ExternalCacheConfig)getConfig()).setKeyPrefix(keyPrefix);
        return self();
    }

    public T valueEncoder(Function<Object, byte[]> valueEncoder){
        ((ExternalCacheConfig)getConfig()).setValueEncoder(valueEncoder);
        return self();
    }

    public T valueDecoder(Function<byte[], Object> valueDecoder){
        ((ExternalCacheConfig)getConfig()).setValueDecoder(valueDecoder);
        return self();
    }
}
