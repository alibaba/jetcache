package com.alicp.jetcache.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheManager {

    private ConcurrentHashMap<String, Cache> caches;

    private static CacheManager instance = new CacheManager();

    public static CacheManager defaultInstance(){
        return instance;
    }

    public Cache getCache(String subArea) {
        return caches.get(subArea);
    }

    public void addCache(String subArea, Cache cache) {
        caches.put(subArea, cache);
    }

}
