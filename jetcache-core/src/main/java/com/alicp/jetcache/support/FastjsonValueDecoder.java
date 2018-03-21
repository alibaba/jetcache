package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Deprecated
public class FastjsonValueDecoder extends AbstractValueDecoder {

    @SuppressWarnings("deprecation")
    public static final FastjsonValueDecoder INSTANCE = new FastjsonValueDecoder();

    @Override
    public Object doApply(byte[] buffer) {
        //noinspection deprecation
        byte[] bs = new byte[buffer.length - 4];
        System.arraycopy(buffer, 4, bs, 0, bs.length);
        return JSON.parse(bs);
    }
}
