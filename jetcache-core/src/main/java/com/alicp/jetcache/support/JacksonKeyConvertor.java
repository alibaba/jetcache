/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JacksonKeyConvertor implements Function<Object, Object> {

    public static final JacksonKeyConvertor INSTANCE = new JacksonKeyConvertor();

    private static ObjectMapper objectMapper = new ObjectMapper();

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
        } catch (JsonProcessingException e) {
            throw new CacheEncodeException("jackson key convert fail", e);
        }
    }

}

