/**
 * Created on  13-10-08 10:12
 */
package com.alicp.jetcache.anno;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface CacheConsts {
    String DEFAULT_AREA = "default";
    String DEFAULT_NAME = "";
    boolean DEFAULT_ENABLED = true;
    int DEFAULT_EXPIRE = 600;
    CacheType DEFAULT_CACHE_TYPE = CacheType.REMOTE;
    int DEFAULT_LOCAL_LIMIT = 100;
    boolean DEFAULT_CACHE_NULL_VALUE = false;
    String DEFAULT_CONDITION = "";
    String DEFAULT_UNLESS = "";
    String DEFAULT_SERIAL_POLICY = SerialPolicy.KRYO;
}
