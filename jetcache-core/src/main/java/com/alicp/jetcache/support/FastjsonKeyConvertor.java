/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;

import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class FastjsonKeyConvertor implements Function<Object, Object> {

    public static final FastjsonKeyConvertor INSTANCE = new FastjsonKeyConvertor();

    public Object apply(Object originalKey) {
        if (originalKey instanceof String) {
            return originalKey;
        }
        if (originalKey instanceof byte[]) {
            return originalKey;
        }
        return JSON.toJSONString(originalKey);
    }

}

