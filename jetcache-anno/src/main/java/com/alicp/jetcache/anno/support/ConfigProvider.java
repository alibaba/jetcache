package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.*;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ConfigProvider {

    public void parseEncoderAndDecoder(ExternalCacheBuilder cacheBuilder, String serialPolicy) {
        if (SerialPolicy.KRYO.equals(serialPolicy)) {
            cacheBuilder.setValueEncoder(KryoValueEncoder.INSTANCE);
            cacheBuilder.setValueDecoder(KryoValueDecoder.INSTANCE);
        } else if (SerialPolicy.JAVA.equals(serialPolicy)) {
            cacheBuilder.setValueEncoder(JavaValueEncoder.INSTANCE);
            cacheBuilder.setValueDecoder(JavaValueDecoder.INSTANCE);
        } else if (SerialPolicy.FASTJSON.equals(serialPolicy)) {
            //noinspection deprecation
            cacheBuilder.setValueEncoder(FastjsonValueEncoder.INSTANCE);
            //noinspection deprecation
            cacheBuilder.setValueDecoder(FastjsonValueDecoder.INSTANCE);
        } else if (serialPolicy != null) {
            throw new CacheException(serialPolicy);
        }
    }

    public CacheContext newContext(GlobalCacheConfig globalCacheConfig) {
        return new CacheContext(globalCacheConfig);
    }
}
