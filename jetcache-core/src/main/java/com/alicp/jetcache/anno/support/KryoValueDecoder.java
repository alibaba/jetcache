package com.alicp.jetcache.anno.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import java.io.ByteArrayInputStream;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class KryoValueDecoder extends AbstractValueDecoder {


    @Override
    public Object apply(byte[] buffer) {
        checkHeader(buffer, KryoValueEncoder.IDENTITY_NUMBER);
        ByteArrayInputStream in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
        Input input = new Input(in);
        Kryo kryo = KryoValueEncoder.kryoThreadLocal.get();
        return kryo.readClassAndObject(input);
    }
}
