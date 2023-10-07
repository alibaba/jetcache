package com.alicp.jetcache.support;

import com.alicp.jetcache.ObjectPool;
import com.alicp.jetcache.anno.SerialPolicy;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class KryoValueEncoder extends AbstractValueEncoder {

    public static final KryoValueEncoder INSTANCE = new KryoValueEncoder(true);

    private static final int INIT_BUFFER_SIZE = 256;

    //Default size = 1M
    static ObjectPool<KryoCache> kryoCacheObjectPool = new ObjectPool<>(1024*1024/INIT_BUFFER_SIZE, new ObjectPool.ObjectFactory<KryoCache>() {
        @Override
        public KryoCache create() {
            return new KryoCache();
        }

        @Override
        public void reset(KryoCache obj) {
            obj.getKryo().reset();
            Arrays.fill(obj.buffer, (byte) 0);
        }
    });

    public static class KryoCache {
        final byte[] buffer;
        final Kryo kryo;
        public KryoCache(){
            kryo = new Kryo();
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            buffer = new byte[INIT_BUFFER_SIZE];
        }
        public byte[] getBuffer(){
            return buffer;
        }
        public Kryo getKryo(){
            return kryo;
        }
    }

    public KryoValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        KryoCache kryoCache = null;
        try {
            kryoCache = kryoCacheObjectPool.borrowObject();
            Output output = new Output(kryoCache.getBuffer(), -1);
            if (useIdentityNumber) {
                writeInt(output, SerialPolicy.IDENTITY_NUMBER_KRYO4);
            }
            kryoCache.getKryo().writeClassAndObject(output, value);
            return output.toBytes();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        } finally {
            if(kryoCache != null)
                kryoCacheObjectPool.returnObject(kryoCache);
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
