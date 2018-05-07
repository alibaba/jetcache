package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import java.lang.ref.WeakReference;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class KryoValueEncoder extends AbstractValueEncoder {

    public static final KryoValueEncoder INSTANCE = new KryoValueEncoder(true);

    protected static int IDENTITY_NUMBER = 0x4A953A82;

    private static int INIT_BUFFER_SIZE = 256;

    static ThreadLocal<Object[]> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
//        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
//        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        Output output = new Output(INIT_BUFFER_SIZE, -1);

        WeakReference<Output> ref = new WeakReference<>(output);
        return new Object[]{kryo, ref};
    });

    public KryoValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        try {
            Object[] kryoAndOutput = kryoThreadLocal.get();
            Kryo kryo = (Kryo) kryoAndOutput[0];
            WeakReference<Output> ref = (WeakReference<Output>) kryoAndOutput[1];
            Output output = ref.get();
            if (output == null) {
                output = new Output(INIT_BUFFER_SIZE, -1);
                kryoAndOutput[1] = new WeakReference<>(output);
            }

            if (useIdentityNumber) {
                output.writeInt(IDENTITY_NUMBER);
            }
            try {
                kryo.writeClassAndObject(output, value);
                return output.toBytes();
            } finally {
                //reuse buffer if possible
                output.clear();
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }

}
