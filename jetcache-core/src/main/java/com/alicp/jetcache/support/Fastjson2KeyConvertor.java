/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;

import com.alibaba.fastjson2.JSON;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class Fastjson2KeyConvertor implements Function<Object, Object> {

    public static final Fastjson2KeyConvertor INSTANCE = new Fastjson2KeyConvertor();

    @Override
    public Object apply(Object originalKey) {
        if (originalKey == null) {
            return null;
        }
        if (originalKey instanceof String) {
            return originalKey;
        }
        return JSON.toJSONString(originalKey);
    }

}

