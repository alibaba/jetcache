package com.alicp.jetcache.external;

import com.alicp.jetcache.AbstractCacheBuilder;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class ExternalCacheBuilder<T extends ExternalCacheBuilder<T>> extends AbstractCacheBuilder<T> {

    @Override
    public ExternalCacheConfig getConfig() {
        if (config == null) {
            config = new ExternalCacheConfig();
        }
        return (ExternalCacheConfig) config;
    }

    public T keyPrefix(String keyPrefix){
        getConfig().setKeyPrefixSupplier(() -> keyPrefix);
        return self();
    }

    public T keyPrefixSupplier(Supplier<String> keyPrefixSupplier) {
        getConfig().setKeyPrefixSupplier(keyPrefixSupplier);
        return self();
    }

    public T valueEncoder(Function<Object, byte[]> valueEncoder){
        getConfig().setValueEncoder(valueEncoder);
        return self();
    }

    public T valueDecoder(Function<byte[], Object> valueDecoder){
        getConfig().setValueDecoder(valueDecoder);
        return self();
    }

    public void setKeyPrefix(String keyPrefix){
        getConfig().setKeyPrefixSupplier(() -> keyPrefix);
    }

    public void setKeyPrefixSupplier(Supplier<String> keyPrefixSupplier){
        getConfig().setKeyPrefixSupplier(keyPrefixSupplier);
    }

    public void setValueEncoder(Function<Object, byte[]> valueEncoder){
        getConfig().setValueEncoder(valueEncoder);
    }

    public void setValueDecoder(Function<byte[], Object> valueDecoder){
        getConfig().setValueDecoder(valueDecoder);
    }
}
