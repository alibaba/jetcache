package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JavaValueEncoder extends AbstractValueEncoder {

    public static final JavaValueEncoder INSTANCE = new JavaValueEncoder(true);

    private static final int INIT_BUF_SIZE = 256;

    public JavaValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    private static ThreadLocal<WeakReference<ByteArrayOutputStream>> threadLocal =
            ThreadLocal.withInitial(() -> new WeakReference<>(new ByteArrayOutputStream(INIT_BUF_SIZE)));

    @Override
    public byte[] apply(Object value) {
        try {
            WeakReference<ByteArrayOutputStream> ref = threadLocal.get();
            ByteArrayOutputStream bos = ref.get();
            if (bos == null) {
                bos = new ByteArrayOutputStream(INIT_BUF_SIZE);
                threadLocal.set(new WeakReference<>(bos));
            }

            try {
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
            } finally {
                bos.reset();
            }
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Java Encode error. ");
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }
}
