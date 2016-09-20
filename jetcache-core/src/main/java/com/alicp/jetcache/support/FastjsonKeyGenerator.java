/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;
import com.alicp.jetcache.cache.KeyGenerator;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class FastjsonKeyGenerator implements KeyGenerator {


    public String getKey(Object... originalKey) {
        return JSON.toJSONString(originalKey);
    }

}

