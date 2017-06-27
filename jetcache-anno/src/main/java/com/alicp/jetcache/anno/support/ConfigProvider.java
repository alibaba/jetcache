package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.anno.KeyConvertor;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.support.*;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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
        } else {
            throw new CacheConfigException("not supported:" + valueDecoder);
        }
    }

    public Function<Object, Object> parseKeyConvertor(String convertor) {
        if (convertor == null) {
            return null;
        }
        if (KeyConvertor.FASTJSON.equalsIgnoreCase(convertor)) {
            return FastjsonKeyConvertor.INSTANCE;
        } else if (KeyConvertor.NONE.equalsIgnoreCase(convertor)) {
            return null;
        }
        throw new CacheConfigException("not supported:" + convertor);
    }

    public CacheContext newContext(GlobalCacheConfig globalCacheConfig) {
        return new CacheContext(globalCacheConfig);
    }

    public Consumer<StatInfo> statCallback() {
        return new StatInfoLogger(false);
    }
}
