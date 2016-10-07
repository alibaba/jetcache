/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;

import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class FastjsonKeyGenerator implements Function<Object, Object> {

    public static FastjsonKeyGenerator INSTANCE = new FastjsonKeyGenerator();

    public Object apply(Object originalKey) {
        return JSON.toJSONString(originalKey);
    }

}

