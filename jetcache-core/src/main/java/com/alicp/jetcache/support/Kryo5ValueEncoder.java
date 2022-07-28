package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.CompatibleFieldSerializer;

import java.lang.ref.WeakReference;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class Kryo5ValueEncoder extends AbstractValueEncoder {

    public static final Kryo5ValueEncoder INSTANCE = new Kryo5ValueEncoder(true);

    private static int INIT_BUFFER_SIZE = 256;

    static ThreadLocal<Object[]> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        kryo.setRegistrationRequired(false);

        Output output = new Output(INIT_BUFFER_SIZE, -1);

        WeakReference<Output> ref = new WeakReference<>(output);
        return new Object[]{kryo, ref};
    });

    public Kryo5ValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        try {
            Object[] kryoAndBuffer = kryoThreadLocal.get();
            Kryo kryo = (Kryo) kryoAndBuffer[0];
            WeakReference<Output> ref = (WeakReference<Output>) kryoAndBuffer[1];
            Output output = ref.get();
            if (output == null) {
                output = new Output(INIT_BUFFER_SIZE, -1);
            }

            try {
                if (useIdentityNumber) {
                    writeInt(output, SerialPolicy.IDENTITY_NUMBER_KRYO5);
                }
                kryo.reset();
                kryo.writeClassAndObject(output, value);
                return output.toBytes();
            } finally {
                //reuse buffer if possible
                output.reset();
                if (ref.get() == null) {
                    ref = new WeakReference<>(output);
                    kryoAndBuffer[1] = ref;
                }
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }

    private void writeInt(Output output, int value) {
        // kryo5 change writeInt to little endian, so we write int manually
        output.writeByte(value >>> 24);
        output.writeByte(value >>> 16);
        output.writeByte(value >>> 8);
        output.writeByte(value);
    }

}
