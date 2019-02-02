/**
 * Created on 2019/2/1.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface CacheManager {
    Cache getCache(String area, String cacheName);

    default Cache getCache(String cacheName) {
        return getCache(CacheConsts.DEFAULT_AREA, cacheName);
    }

    static CacheManager defaultManager() {
        return SimpleCacheManager.defaultManager;
    }
}
