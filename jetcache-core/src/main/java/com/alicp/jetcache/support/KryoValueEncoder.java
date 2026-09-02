package com.alicp.jetcache.support;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.MapReferenceResolver;

/**
 * Created on 2016/10/4.
 *
 * Since 2.8.0 the com.esotericsoftware:kryo should be 5+, kryo4 is not supported.
 *
 * @author huangli
 */
public class KryoValueEncoder extends AbstractValueEncoder {

    public static final ObjectPool<KryoCache> DEFAULT_POOL = new ObjectPool<>(16,
            new KryoCacheFactory(DecodeFilter.getDefault()));

    public static final KryoValueEncoder INSTANCE = new KryoValueEncoder(true, DEFAULT_POOL);

    private static final int INIT_BUFFER_SIZE = 2048;

    private final ObjectPool<KryoCache> pool;

    public static class KryoCacheFactory implements ObjectPool.ObjectFactory<KryoCache> {
        private final DecodeFilter decodeFilter;
        public KryoCacheFactory(DecodeFilter decodeFilter) {
            this.decodeFilter = decodeFilter;
        }

        @Override
        public KryoCache create() {
            return new KryoCache(decodeFilter);
        }

        @Override
        public void reset(KryoCache obj) {
            obj.getKryo().reset();
            obj.getOutput().reset();
        }
    }

    public static class KryoCache {
        final Output output;
        final Kryo kryo;
        public KryoCache(DecodeFilter decodeFilter) {
            kryo = new Kryo(new KryoClassResolver(decodeFilter), new MapReferenceResolver());
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            kryo.setRegistrationRequired(false);
            output = new Output(INIT_BUFFER_SIZE, -1);
        }

        public Output getOutput() {
            return output;
        }

        public Kryo getKryo(){
            return kryo;
        }

    }

    public KryoValueEncoder(boolean useIdentityNumber, ObjectPool<KryoCache> pool) {
        super(useIdentityNumber);
        this.pool = pool;
    }

    @Override
    public byte[] apply(Object value) {
        KryoCache kryoCache = null;
        try {
            kryoCache = pool.borrowObject();
            if (useIdentityNumber) {
                writeInt(kryoCache.getOutput(), DecoderMap.IDENTITY_NUMBER_KRYO5);
            }
            kryoCache.getKryo().writeClassAndObject(kryoCache.getOutput(), value);
            return kryoCache.getOutput().toBytes();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Kryo Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        } finally {
            if (kryoCache != null) {
                pool.returnObject(kryoCache);
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
