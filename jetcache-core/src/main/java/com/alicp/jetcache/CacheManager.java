/**
 * Created on 2019/2/1.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface CacheManager {
    Cache getCache(String area, String cacheName);

    void putCache(String area, String cacheName, Cache cache);

    default Cache getCache(String cacheName) {
        return getCache(CacheConsts.DEFAULT_AREA, cacheName);
    }

    default void putCache(String cacheName, Cache cache){
        putCache(CacheConsts.DEFAULT_AREA, cacheName, cache);
    }

    static CacheManager defaultManager() {
        return SimpleCacheManager.defaultManager;
    }
}
