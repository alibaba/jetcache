package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

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
    private static ThreadLocal<Output> outputThreadLocal = ThreadLocal.withInitial(() -> new Output(256, -1));

    public KryoValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        try {
            Kryo kryo = kryoThreadLocal.get();
            Output output = outputThreadLocal.get();
            if (useIdentityNumber) {
                output.writeInt(IDENTITY_NUMBER);
            }
            try {
                kryo.writeClassAndObject(output, value);
                return output.toBytes();
            } finally {
                if(output != null){
                    output.clear();
                }
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }
}
