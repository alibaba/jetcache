/**
 * Created on 2019/2/1.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.support.BroadcastManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SimpleCacheManager implements CacheManager, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheManager.class);

    // area -> cacheName -> Cache
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Cache>> caches = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, BroadcastManager> broadcastManagers = new ConcurrentHashMap();

    static final SimpleCacheManager defaultManager = new SimpleCacheManager();

    public SimpleCacheManager() {
    }

    @Override
    public void close() {
        broadcastManagers.forEach((area, bm) -> {
            try {
                bm.close();
            } catch (Exception e) {
                logger.error("error during close broadcast manager", e);
            }
        });
        broadcastManagers.clear();
        caches.forEach((area, areaMap) -> {
            areaMap.forEach((cacheName, cache) -> {
                try {
                    cache.close();
                } catch (Exception e) {
                    logger.error("error during close Cache", e);
                }
            });
        });
        caches.clear();
    }

    private ConcurrentHashMap<String, Cache> getCachesByArea(String area) {
        return caches.computeIfAbsent(area, (key) -> new ConcurrentHashMap<>());
    }

    @Override
    public Cache getCache(String area, String cacheName) {
        ConcurrentHashMap<String, Cache> areaMap = getCachesByArea(area);
        return areaMap.get(cacheName);
    }

    @Override
    public void putCache(String area, String cacheName, Cache cache) {
        ConcurrentHashMap<String, Cache> areaMap = getCachesByArea(area);
        areaMap.put(cacheName, cache);
    }

    @Override
    public BroadcastManager getBroadcastManager(String area) {
        return broadcastManagers.get(area);
    }

    @Override
    public void putBroadcastManager(String area, BroadcastManager broadcastManager) {
        broadcastManagers.put(area, broadcastManager);
    }
}
