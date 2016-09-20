package com.alicp.jetcache.cache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface KeyGenerator {

    Object getKey(Object originalKey);

}
