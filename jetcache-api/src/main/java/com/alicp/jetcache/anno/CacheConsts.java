/**
 * Created on  13-10-08 10:12
 */
package com.alicp.jetcache.anno;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.SerialPolicy;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface CacheConsts {
    String DEFAULT_AREA = "";
    boolean DEFAULT_ENABLED = true;
    int DEFAULT_EXPIRE = 600;
    CacheType DEFAULT_CACHE_TYPE = CacheType.REMOTE;
    int DEFAULT_LOCAL_LIMIT = 100;
    int DEFAULT_VERSION = 1;
    boolean DEFAULT_CACHE_NULL_VALUE = false;
    String DEFAULT_CONDITION = "";
    String DEFAULT_UNLESS = "";
    SerialPolicy DEFAULT_SERIAL_POLICY = SerialPolicy.KRYO;
}
