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

    static ThreadLocal<WeakReference<KryoCache>> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        KryoCache kryo = new KryoCache();
        WeakReference<KryoCache> ref = new WeakReference<>(kryo);
        return ref;
    });

    static class KryoCache{
        final Output output;
        final Kryo kryo;
        public KryoCache(){
            kryo = new Kryo();
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            kryo.setRegistrationRequired(false);
            output = new Output(INIT_BUFFER_SIZE, -1);
        }

        public Output getOutput(){
            return output;
        }

        public Kryo getKryo(){
            return kryo;
        }

    }

    public Kryo5ValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        KryoCache kryoCache = null;
        try {
            WeakReference<KryoCache> weakRef = kryoThreadLocal.get();
            if(weakRef == null || weakRef.get() == null){
                kryoCache = new KryoCache();
                weakRef = new WeakReference<>(kryoCache);
                kryoThreadLocal.set(weakRef);
            }else{
                kryoCache = weakRef.get();
            }
            try {
                if (useIdentityNumber) {
                    writeInt(kryoCache.getOutput(), SerialPolicy.IDENTITY_NUMBER_KRYO5);
                }
                kryoCache.getKryo().reset();
                kryoCache.getKryo().writeClassAndObject(kryoCache.getOutput(), value);
                return kryoCache.getOutput().toBytes();
            } finally {
                //reuse buffer if possible
                if(kryoCache != null) {
                    kryoCache.getOutput().reset();
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
