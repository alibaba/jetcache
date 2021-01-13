/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class FastjsonKeyConvertor implements Function<Object, Object> {

    public static final FastjsonKeyConvertor INSTANCE = new FastjsonKeyConvertor();

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

