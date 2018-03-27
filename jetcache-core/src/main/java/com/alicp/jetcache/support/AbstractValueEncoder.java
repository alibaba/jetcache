package com.alicp.jetcache.support;

import java.util.function.Function;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractValueEncoder implements Function<Object, byte[]> {

    protected boolean useIdentityNumber;

    protected void writeHeader(byte[] buf, int header) {
        buf[0] = (byte) (header >> 24 & 0xFF);
        buf[1] = (byte) (header >> 16 & 0xFF);
        buf[2] = (byte) (header >> 8 & 0xFF);
        buf[3] = (byte) (header & 0xFF);
    }

    public AbstractValueEncoder(boolean useIdentityNumber) {
        this.useIdentityNumber = useIdentityNumber;
    }

    public boolean isUseIdentityNumber() {
        return useIdentityNumber;
    }
}
