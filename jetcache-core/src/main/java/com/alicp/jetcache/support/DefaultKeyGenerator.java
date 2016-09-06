/**
 * Created on  13-09-10 15:45
 */
package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultKeyGenerator implements KeyGenerator {

    @Override
    public String getKey(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            sb.append(JSON.toJSONString(arg));
            if (i < args.length - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

}

