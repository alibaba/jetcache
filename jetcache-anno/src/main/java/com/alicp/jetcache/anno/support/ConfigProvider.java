package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.*;

import java.util.function.Function;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ConfigProvider {

    public Function<Object, byte[]> parseValueEncoder(String valueEncoder) {
        if (valueEncoder == null) {
            throw new CacheConfigException("no serialPolicy");
        }
        valueEncoder = valueEncoder.trim();
        if (SerialPolicy.KRYO.equalsIgnoreCase(valueEncoder)) {
            return KryoValueEncoder.INSTANCE;
        } else if (SerialPolicy.JAVA.equalsIgnoreCase(valueEncoder)) {
            return JavaValueEncoder.INSTANCE;
        } else if (SerialPolicy.FASTJSON.equalsIgnoreCase(valueEncoder)) {
            //noinspection deprecation
            return FastjsonValueEncoder.INSTANCE;
        } else {
            throw new CacheConfigException("not supported:" + valueEncoder);
        }
    }

    public Function<byte[], Object> parseValueDecoder(String valueDecoder) {
        if (valueDecoder == null) {
            throw new CacheConfigException("no serialPolicy");
        }
        valueDecoder = valueDecoder.trim();
        if (SerialPolicy.KRYO.equalsIgnoreCase(valueDecoder)) {
            return KryoValueDecoder.INSTANCE;
        } else if (SerialPolicy.JAVA.equalsIgnoreCase(valueDecoder)) {
            return JavaValueDecoder.INSTANCE;
        } else if (SerialPolicy.FASTJSON.equalsIgnoreCase(valueDecoder)) {
            //noinspection deprecation
            return FastjsonValueDecoder.INSTANCE;
        } else {
            throw new CacheConfigException("not supported:" + valueDecoder);
        }
    }

    public Function<Object, Object> parseKeyConvertor(String convertor) {
        if (convertor == null) {
            return null;
        }
        if ("fastjson".equalsIgnoreCase(convertor)) {
            return FastjsonKeyConvertor.INSTANCE;
        }
        throw new CacheConfigException("not supported:" + convertor);
    }

    public CacheContext newContext(GlobalCacheConfig globalCacheConfig) {
        return new CacheContext(globalCacheConfig);
    }
}
