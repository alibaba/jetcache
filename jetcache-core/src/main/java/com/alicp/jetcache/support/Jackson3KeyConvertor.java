/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;




import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.function.Function;

/**
 * @author zhangtong2
 */
public class Jackson3KeyConvertor implements Function<Object, Object> {

    public static final Jackson3KeyConvertor INSTANCE = new Jackson3KeyConvertor();

    private static final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Override
    public Object apply(Object originalKey) {
        if (originalKey == null) {
            return null;
        }
        if (originalKey instanceof CharSequence) {
            return originalKey.toString();
        }
        try {
            return objectMapper.writeValueAsString(originalKey);
        } catch (JacksonException e) {
            throw new CacheEncodeException("jackson3 key convert fail", e);
        }
    }

}

