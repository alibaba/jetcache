package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheManager {

    private ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap();

    private static CacheManager instance = new CacheManager();

    public static CacheManager defaultInstance(){
        return instance;
    }

    public Cache getCache(String key) {
        return caches.get(key);
    }

    public void addCache(String key, Cache cache) {
        caches.put(key, cache);
    }

}
