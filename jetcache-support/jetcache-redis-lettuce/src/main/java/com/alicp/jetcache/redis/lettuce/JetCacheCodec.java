package com.alicp.jetcache.redis.lettuce;


import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JetCacheCodec implements RedisCodec {

    @Override
    public ByteBuffer encodeKey(Object key) {
        byte[] bytes = (byte[]) key;
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public Object decodeKey(ByteBuffer bytes) {
        return convert(bytes);
    }

    @Override
    public ByteBuffer encodeValue(Object value) {
        byte[] bytes = (byte[]) value;
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public Object decodeValue(ByteBuffer bytes) {
        return convert(bytes);
    }

    private Object convert(ByteBuffer bytes){
        byte[] bs = new byte[bytes.remaining()];
        bytes.get(bs);
        return bs;
    }


}
