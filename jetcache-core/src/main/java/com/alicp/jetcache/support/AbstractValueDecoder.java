package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheException;

import java.util.function.Function;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractValueDecoder implements Function<byte[], Object> {

    protected int parseHeader(byte[] buf){
        int x = 0;
        x = x | (buf[0] & 0xFF);
        x <<= 8;
        x = x | (buf[1] & 0xFF);
        x <<= 8;
        x = x | (buf[2] & 0xFF);
        x <<= 8;
        x = x | (buf[3] & 0xFF);
        return x;
    }

    protected void checkHeader(byte[] buf, int expectedHeader) {
        int x = parseHeader(buf);
        if(x != expectedHeader){
            throw new CacheException("unexpected header:" + Integer.toHexString(x) + ",expect " + Integer.toHexString(expectedHeader));
        }
    }
}
