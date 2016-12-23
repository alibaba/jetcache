package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import java.io.ByteArrayInputStream;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class KryoValueDecoder extends AbstractValueDecoder {

    public static final KryoValueDecoder INSTANCE = new KryoValueDecoder();

    @Override
    public Object apply(byte[] buffer) {
        try {
            checkHeader(buffer, KryoValueEncoder.IDENTITY_NUMBER);
            ByteArrayInputStream in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
            Input input = new Input(in);
            Kryo kryo = KryoValueEncoder.kryoThreadLocal.get();
            return kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new CacheEncodeException("Kryo decode error: " + e.getMessage(), e);
        }
    }
}
