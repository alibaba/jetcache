/**
 * Created on 2019/2/1.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SimpleCacheManager implements CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheManager.class);

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Cache>> caches = new ConcurrentHashMap<>();
    private BiFunction<String, String, Cache> cacheCreator;

    static SimpleCacheManager defaultManager = new SimpleCacheManager();

    public SimpleCacheManager() {
    }

    public void rebuild() {
        caches.forEach((area, areaMap) -> {
            areaMap.forEach((cacheName, cache) -> {
                try {
                    cache.close();
                } catch (Exception e) {
                    logger.error("error during close", e);
                }
            });
        });
        caches.clear();
        cacheCreator = null;
    }

    private ConcurrentHashMap<String, Cache> getCachesByArea(String area) {
        return caches.computeIfAbsent(area, (key) -> new ConcurrentHashMap<>());
    }

    @Override
    public Cache getCache(String area, String cacheName) {
        ConcurrentHashMap<String, Cache> areaMap = getCachesByArea(area);
        Cache c = areaMap.get(cacheName);
        if (c == null && cacheCreator != null) {
            return cacheCreator.apply(area, cacheName);
        } else {
            return c;
        }
    }

    public Cache getCacheWithoutCreate(String area, String cacheName) {
        ConcurrentHashMap<String, Cache> areaMap = getCachesByArea(area);
        return areaMap.get(cacheName);
    }

    public void putCache(String area, String cacheName, Cache cache) {
        ConcurrentHashMap<String, Cache> areaMap = getCachesByArea(area);
        areaMap.put(cacheName, cache);
    }

    public void setCacheCreator(BiFunction<String, String, Cache> cacheCreator) {
        this.cacheCreator = cacheCreator;
    }
}
