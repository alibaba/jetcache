package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.CompatibleFieldSerializer;

/**
 * Created on 2016/10/4.
 *
 * @author huangli
 */
public class Kryo5ValueEncoder extends AbstractValueEncoder {

    public static final Kryo5ValueEncoder INSTANCE = new Kryo5ValueEncoder(true);

    private static final int INIT_BUFFER_SIZE = 2048;

    //Default size = 32K
    static ObjectPool<Kryo5Cache> kryoCacheObjectPool = new ObjectPool<>(16, new ObjectPool.ObjectFactory<Kryo5Cache>() {
        @Override
        public Kryo5Cache create() {
            return new Kryo5Cache();
        }

        @Override
        public void reset(Kryo5Cache obj) {
            obj.getKryo().reset();
            obj.getOutput().reset();
        }
    });

    public static class Kryo5Cache {
        final Output output;
        final Kryo kryo;
        public Kryo5Cache(){
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
        Kryo5Cache kryoCache = null;
        try {
            kryoCache = kryoCacheObjectPool.borrowObject();
            if (useIdentityNumber) {
                writeInt(kryoCache.getOutput(), SerialPolicy.IDENTITY_NUMBER_KRYO5);
            }
            kryoCache.getKryo().writeClassAndObject(kryoCache.getOutput(), value);
            return kryoCache.getOutput().toBytes();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        } finally {
            if (kryoCache != null) {
                kryoCacheObjectPool.returnObject(kryoCache);
            }
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
