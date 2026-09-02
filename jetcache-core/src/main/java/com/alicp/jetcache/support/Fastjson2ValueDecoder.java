package com.alicp.jetcache.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;

import java.nio.charset.StandardCharsets;

/**
 * Created on 2022/07/26.
 *
 * @author huangli
 */
public class Fastjson2ValueDecoder extends AbstractJsonDecoder {

    public static final Fastjson2ValueDecoder INSTANCE = new Fastjson2ValueDecoder(true);

    private static final JSONReader.AutoTypeBeforeHandler AUTO_TYPE_HANDLER =
            (typeName, expectClass, features) -> {
                if (!DecodeFilter.getDefault().isAllowed(typeName)) {
                    throw new DecodeFilterException(typeName);
                }
                return null;
            };

    public Fastjson2ValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    protected Object parseObject(byte[] buffer, int index, int len, Class clazz) {
        String s = new String(buffer, index, len, StandardCharsets.UTF_8);
        return JSON.parseObject(s, clazz, AUTO_TYPE_HANDLER, JSONReader.Feature.SupportAutoType);
    }

}
