package com.alicp.jetcache.anno.support;

import java.util.function.Function;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractValueEncoder implements Function<Object, byte[]> {

    protected void writeHeader(byte[] buf, int header) {
        buf[0] = (byte) (header >>> 24);
        buf[1] = (byte) (header >>> 16);
        buf[2] = (byte) (header >>> 8);
        buf[3] = (byte) header;
    }
}
