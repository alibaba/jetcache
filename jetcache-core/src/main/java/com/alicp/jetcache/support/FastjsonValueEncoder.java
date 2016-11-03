package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Created on 2016/10/3.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Deprecated
public class FastjsonValueEncoder extends AbstractValueEncoder {

    @SuppressWarnings("deprecation")
    public static final FastjsonValueEncoder INSTANCE = new FastjsonValueEncoder();

    protected static int IDENTITY_NUMBER = 0x4A953A81;

    @Override
    public byte[] apply(Object o) {
        byte[] bs1 = JSON.toJSONBytes(o, SerializerFeature.WriteClassName);
        byte[] bs2 = new byte[bs1.length + 4];
        writeHeader(bs2, IDENTITY_NUMBER);
        System.arraycopy(bs1, 0, bs2, 4, bs1.length);
        return bs2;
    }
}
