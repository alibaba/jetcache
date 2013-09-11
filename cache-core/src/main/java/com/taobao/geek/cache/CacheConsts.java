/**
 * Created on  13-09-10 14:56
 */
package com.taobao.geek.cache;

/**
 * @author yeli.hl
 */
public interface CacheConsts {
    String DEFAULT_AREA = "";
    boolean DEFAULT_ENABLED = true;
    int DEFAULT_EXPIRE = 600;
    /**
     * 注意修改这里对Cached注解无效
     */
    CacheType DEFAULT_CACHE_TYPE = CacheType.REMOTE;
    int DEFAULT_LOCAL_LIMIT = 100;

    int DEFAULT_VERSION = 1;
}
