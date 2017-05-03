package com.alicp.jetcache.redis.luttece;

import com.lambdaworks.redis.codec.RedisCodec;

import java.nio.ByteBuffer;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
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
        int pos =  bytes.position();
        System.arraycopy(bytes.array(), pos, bs, 0, bs.length);
        bytes.position(pos + bs.length);
        return bs;
    }


}
