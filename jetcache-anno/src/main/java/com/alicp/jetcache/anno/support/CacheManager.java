package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheManager {

    private Map<String, Cache> caches = new Hashtable<>();

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
