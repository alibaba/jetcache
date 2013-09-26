/**
 * Created on  13-09-10 14:56
 */
package com.taobao.geek.jetcache;

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
}
