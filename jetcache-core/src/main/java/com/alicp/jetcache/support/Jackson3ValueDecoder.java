package com.alicp.jetcache.support;

import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.nio.charset.StandardCharsets;

/**
 * @author huangli
 */
public class Jackson3ValueDecoder extends AbstractJsonDecoder {

    public static final Jackson3ValueDecoder INSTANCE = new Jackson3ValueDecoder(true);

    static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .activateDefaultTyping(
                    new JetCachePolymorphicTypeValidator(),
                    DefaultTyping.NON_FINAL
            )
            .build();

    public Jackson3ValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    protected Object parseObject(byte[] buffer, int index, int len, Class clazz) {
        String s = new String(buffer, index, len, StandardCharsets.UTF_8);
        return OBJECT_MAPPER.readValue(s, clazz);
    }

    static class JetCachePolymorphicTypeValidator extends PolymorphicTypeValidator.Base {
        @Override
        public Validity validateSubClassName(DatabindContext ctxt, JavaType baseType, String subClassName) {
            if (!DecodeFilter.getDefault().isAllowed(subClassName)) {
                throw new DecodeFilterException(subClassName);
            }
            return Validity.ALLOWED;
        }

        @Override
        public Validity validateSubType(DatabindContext ctxt, JavaType baseType, JavaType subType) {
            String className = subType.getRawClass().getName();
            if (!DecodeFilter.getDefault().isAllowed(className)) {
                throw new DecodeFilterException(className);
            }
            return Validity.ALLOWED;
        }
    }
}
