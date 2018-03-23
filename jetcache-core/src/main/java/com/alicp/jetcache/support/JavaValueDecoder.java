package com.alicp.jetcache.support;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JavaValueDecoder extends AbstractValueDecoder {

    public static final JavaValueDecoder INSTANCE = new JavaValueDecoder(true);

    public JavaValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public Object doApply(byte[] buffer) throws Exception {
        ByteArrayInputStream in;
        if (useIdentityNumber) {
            in = new ByteArrayInputStream(buffer, 4, buffer.length - 4);
        } else {
            in = new ByteArrayInputStream(buffer);
        }
        ObjectInputStream ois = new ObjectInputStream(in);
        return ois.readObject();
    }
}
