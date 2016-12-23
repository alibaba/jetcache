package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class JavaValueEncoder extends AbstractValueEncoder {

    public static final JavaValueEncoder INSTANCE = new JavaValueEncoder();

    protected static int IDENTITY_NUMBER = 0x4A953A80;

    @Override
    public byte[] apply(Object value) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
            bos.write((IDENTITY_NUMBER >> 24) & 0xFF);
            bos.write((IDENTITY_NUMBER >> 16) & 0xFF);
            bos.write((IDENTITY_NUMBER >> 8) & 0xFF);
            bos.write(IDENTITY_NUMBER & 0xFF);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Java Encode error.");
            if (value != null) {
                sb.append("class=").append(value.getClass().getName()).append('.');
            }
            sb.append("msg=").append(e.getMessage());
            throw new CacheEncodeException(sb.toString(), e);
        }
    }
}
