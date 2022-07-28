package com.alicp.jetcache.support;

import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;

/**
 * Created on 2022/07/26.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class Fastjson2ValueDecoder extends AbstractJsonDecoder {

    public static final Fastjson2ValueDecoder INSTANCE = new Fastjson2ValueDecoder(true);

    public Fastjson2ValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    protected Object parseObject(byte[] buffer, int index, int len, Class clazz) {
        String s = new String(buffer, index, len, StandardCharsets.UTF_8);
        return JSON.parseObject(s, clazz);
    }
}
