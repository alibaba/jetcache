package com.alicp.jetcache.external;

import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.support.DecoderMap;
import com.alicp.jetcache.support.JavaValueEncoder;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ExternalCacheConfig<K, V> extends CacheConfig<K, V> {
    @Deprecated
    private String keyPrefix;

    private Supplier<String> keyPrefixSupplier;
    private Function<Object, byte[]> valueEncoder = JavaValueEncoder.INSTANCE;
    private Function<byte[], Object> valueDecoder = DecoderMap.defaultJavaValueDecoder();

    public String getKeyPrefix() {
        // keyPrefix is higher priority.
        if (keyPrefix == null && keyPrefixSupplier != null) {
            return keyPrefixSupplier.get();
        }
        return keyPrefix;
    }

    @Deprecated
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Supplier<String> getKeyPrefixSupplier() {
        return keyPrefixSupplier;
    }

    public void setKeyPrefixSupplier(Supplier<String> keyPrefixSupplier) {
        this.keyPrefixSupplier = keyPrefixSupplier;
    }

    public Function<Object, byte[]> getValueEncoder() {
        return valueEncoder;
    }

    public void setValueEncoder(Function<Object, byte[]> valueEncoder) {
        this.valueEncoder = valueEncoder;
    }

    public Function<byte[], Object> getValueDecoder() {
        return valueDecoder;
    }

    public void setValueDecoder(Function<byte[], Object> valueDecoder) {
        this.valueDecoder = valueDecoder;
    }
}
