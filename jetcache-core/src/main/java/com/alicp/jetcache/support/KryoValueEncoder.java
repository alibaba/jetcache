package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import java.io.ByteArrayOutputStream;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class KryoValueEncoder extends AbstractValueEncoder {

    public static final KryoValueEncoder INSTANCE = new KryoValueEncoder(true);

    protected static int IDENTITY_NUMBER = 0x4A953A82;

    static ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
//        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
//        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    });

    public KryoValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        try {
            Kryo kryo = kryoThreadLocal.get();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
            Output output = new Output(bos);
            if (useIdentityNumber) {
                output.writeInt(IDENTITY_NUMBER);
            }
            kryo.writeClassAndObject(output, value);
            output.close();
            return bos.toByteArray();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }
}
