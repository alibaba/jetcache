package com.alicp.jetcache.external;

import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;

import java.util.function.Function;

/**
 * Created on 16/9/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExternalCacheConfig extends CacheConfig {
    private String keyPrefix;
    private Function<Object, byte[]> valueEncoder = JavaValueEncoder.INSTANCE;
    private Function<byte[], Object> valueDecoder = JavaValueDecoder.INSTANCE;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
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
