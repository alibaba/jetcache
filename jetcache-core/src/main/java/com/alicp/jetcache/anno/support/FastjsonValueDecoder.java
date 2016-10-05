package com.alicp.jetcache.anno.support;

import com.alibaba.fastjson.JSON;

/**
 * Created on 2016/10/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class FastjsonValueDecoder extends AbstractValueDecoder {

    @Override
    public Object apply(byte[] buffer) {
        checkHeader(buffer, FastjsonValueEncoder.IDENTITY_NUMBER);
        byte[] bs = new byte[buffer.length - 4];
        System.arraycopy(buffer, 4, bs, 0, bs.length);
        return JSON.parse(bs);
    }
}
