package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheException;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class JavaValueDecoder extends AbstractValueDecoder {

    public static final JavaValueDecoder INSTANCE = new JavaValueDecoder();

    @Override
    public Object apply(byte[] buffer) {
        try {
            checkHeader(buffer, JavaValueEncoder.IDENTITY_NUMBER);
            ByteArrayInputStream in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
            ObjectInputStream ois = new ObjectInputStream(in);
            return ois.readObject();
        } catch (Exception e) {
            throw new CacheException("Java decode error: " + e.getMessage(), e);
        }
    }
}
