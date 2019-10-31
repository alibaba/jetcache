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

    private static int INIT_BUFFER_SIZE = 512;

    static ThreadLocal<Object[]> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
//        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
//        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        byte[] buffer = new byte[INIT_BUFFER_SIZE];

        WeakReference<byte[]> ref = new WeakReference<>(buffer);
        return new Object[]{kryo, ref};
    });

    public KryoValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        try {
            Object[] kryoAndBuffer = kryoThreadLocal.get();
            Kryo kryo = (Kryo) kryoAndBuffer[0];
            WeakReference<byte[]> ref = (WeakReference<byte[]>) kryoAndBuffer[1];
            byte[] buffer = ref.get();
            if (buffer == null) {
                buffer = new byte[INIT_BUFFER_SIZE];
            }
            Output output = new Output(buffer, -1);

            try {
                if (useIdentityNumber) {
                    output.writeInt(IDENTITY_NUMBER);
                }
                kryo.writeClassAndObject(output, value);
                return output.toBytes();
            } finally {
                //reuse buffer if possible
                if (ref.get() == null || buffer != output.getBuffer()) {
                    ref = new WeakReference<>(output.getBuffer());
                    kryoAndBuffer[1] = ref;
                }
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }

}
