package com.alicp.jetcache.factory;

import com.alicp.jetcache.external.ExternalCacheConfig;

import java.util.function.Function;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class ExternalCacheFactory extends CacheFactory {
    @Override
    protected ExternalCacheConfig getConfig() {
        if (config == null) {
            config = new ExternalCacheConfig();
        }
        return (ExternalCacheConfig) config;
    }

    public void setKeyPrefix(String keyPrefix){
        getConfig().setKeyPrefix(keyPrefix);
    }

    public void setValueEncoder(Function<Object, byte[]> valueEncoder){
        getConfig().setValueEncoder(valueEncoder);
    }

    public void setValueDecoder(Function<byte[], Object> valueDecoder){
        getConfig().setValueDecoder(valueDecoder);
    }
}
