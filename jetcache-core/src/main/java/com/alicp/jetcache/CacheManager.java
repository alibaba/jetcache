/**
 * Created on 2019/2/1.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.template.QuickConfig;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface CacheManager {
    Cache getCache(String area, String cacheName);

    void putCache(String area, String cacheName, Cache cache);

    BroadcastManager getBroadcastManager(String area);

    void putBroadcastManager(String area, BroadcastManager broadcastManager);

    default Cache getCache(String cacheName) {
        return getCache(CacheConsts.DEFAULT_AREA, cacheName);
    }

    default void putCache(String cacheName, Cache cache){
        putCache(CacheConsts.DEFAULT_AREA, cacheName, cache);
    }

    default BroadcastManager getBroadcastManager(){
        return getBroadcastManager(CacheConsts.DEFAULT_AREA);
    }

    <K, V> Cache<K, V> getOrCreateCache(QuickConfig config);

    default void putBroadcastManager(BroadcastManager broadcastManager){
        putBroadcastManager(CacheConsts.DEFAULT_AREA, broadcastManager);
    }

}
