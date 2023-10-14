package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created on 2016/10/4.
 *
 * @author huangli
 */
public class JavaValueEncoder extends AbstractValueEncoder {

    public static final JavaValueEncoder INSTANCE = new JavaValueEncoder(true);

    private static final int INIT_BUF_SIZE = 2048;

    public JavaValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    static ObjectPool<ByteArrayOutputStream> bosPool = new ObjectPool<>(16, new ObjectPool.ObjectFactory<ByteArrayOutputStream>() {
        @Override
        public ByteArrayOutputStream create() {
            return new ByteArrayOutputStream(INIT_BUF_SIZE);
        }

        @Override
        public void reset(ByteArrayOutputStream obj) {
            obj.reset();
        }
    });

    @Override
    public byte[] apply(Object value) {
        ByteArrayOutputStream bos = null;
        try {
            bos = bosPool.borrowObject();
            if (useIdentityNumber) {
                bos.write((SerialPolicy.IDENTITY_NUMBER_JAVA >> 24) & 0xFF);
                bos.write((SerialPolicy.IDENTITY_NUMBER_JAVA >> 16) & 0xFF);
                bos.write((SerialPolicy.IDENTITY_NUMBER_JAVA >> 8) & 0xFF);
                bos.write(SerialPolicy.IDENTITY_NUMBER_JAVA & 0xFF);
            }
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Java Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        } finally {
            if (bos != null) {
                bosPool.returnObject(bos);
            }
        }
    }
}
