package com.alicp.jetcache.external;

import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.support.DecoderMap;
import com.alicp.jetcache.support.JavaValueEncoder;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 16/9/9.
 *
 * @author huangli
 */
public class ExternalCacheConfig<K, V> extends CacheConfig<K, V> {

    private Supplier<String> keyPrefixSupplier;
    private Function<Object, byte[]> valueEncoder = JavaValueEncoder.INSTANCE;
    private Function<byte[], Object> valueDecoder = DecoderMap.defaultJavaValueDecoder();
    private String broadcastChannel;

    public String getKeyPrefix() {
        return keyPrefixSupplier == null ? null : keyPrefixSupplier.get();
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefixSupplier = () -> keyPrefix;
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

    public String getBroadcastChannel() {
        return broadcastChannel;
    }

    public void setBroadcastChannel(String broadcastChannel) {
        this.broadcastChannel = broadcastChannel;
    }
}
