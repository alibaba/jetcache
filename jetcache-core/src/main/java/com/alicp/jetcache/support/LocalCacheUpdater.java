/**
 * Created on 2022-05-04.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheMonitor;
import com.alicp.jetcache.CacheUtil;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import com.alicp.jetcache.event.CachePutAllEvent;
import com.alicp.jetcache.event.CachePutEvent;
import com.alicp.jetcache.event.CacheRemoveAllEvent;
import com.alicp.jetcache.event.CacheRemoveEvent;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class LocalCacheUpdater implements Consumer<CacheMessage> {
    private final BroadcastManager broadcastManager;
    private final CacheManager cacheManager;
    private volatile boolean closed;

    public LocalCacheUpdater(BroadcastManager broadcastManager, CacheManager cacheManager) {
        this.broadcastManager = broadcastManager;
        this.cacheManager = cacheManager;
    }

    @Override
    public void accept(CacheMessage cacheMessage) {
        if (cacheMessage == null) {
            return;
        }
        Cache cache = cacheManager.getCache(cacheMessage.getArea(), cacheMessage.getCacheName());
        Cache absCache = CacheUtil.getAbstractCache(cache);
        if (!(absCache instanceof MultiLevelCache)) {
            return;
        }
        Cache[] caches = ((MultiLevelCache) absCache).caches();
        Set<Object> keys = Stream.of(cacheMessage.getKeys()).collect(Collectors.toSet());
        for (Cache c : caches) {
            Cache localCache = CacheUtil.getAbstractCache(c);
            if (localCache instanceof AbstractEmbeddedCache) {
                localCache.REMOVE_ALL(keys);
            } else {
                break;
            }
        }
    }

    public void addNotifyMonitor(String area, String cacheName, Cache cache) {
        AbstractCache absCache = CacheUtil.getAbstractCache(cache);
        if (!(absCache instanceof MultiLevelCache)) {
            return;
        }
        CacheMonitor monitor = event -> {
            if (absCache.isClosed()) {
                return;
            }
            if (event instanceof CachePutEvent) {
                CacheMessage m = new CacheMessage();
                m.setArea(area);
                m.setCacheName(cacheName);
                CachePutEvent e = (CachePutEvent) event;
                m.setType(CacheMessage.TYPE_PUT);
                m.setKeys(new Object[]{e.getKey()});
                broadcastManager.publish(m);
            } else if (event instanceof CacheRemoveEvent) {
                CacheMessage m = new CacheMessage();
                m.setArea(area);
                m.setCacheName(cacheName);
                CacheRemoveEvent e = (CacheRemoveEvent) event;
                m.setType(CacheMessage.TYPE_REMOVE);
                m.setKeys(new Object[]{e.getKey()});
                broadcastManager.publish(m);
            } else if (event instanceof CachePutAllEvent) {
                CacheMessage m = new CacheMessage();
                m.setArea(area);
                m.setCacheName(cacheName);
                CachePutAllEvent e = (CachePutAllEvent) event;
                m.setType(CacheMessage.TYPE_PUT_ALL);
                if (e.getMap() != null) {
                    m.setKeys(e.getMap().keySet().toArray());
                }
                broadcastManager.publish(m);
            } else if (event instanceof CacheRemoveAllEvent) {
                CacheMessage m = new CacheMessage();
                m.setArea(area);
                m.setCacheName(cacheName);
                CacheRemoveAllEvent e = (CacheRemoveAllEvent) event;
                m.setType(CacheMessage.TYPE_REMOVE_ALL);
                if (e.getKeys() != null) {
                    m.setKeys(e.getKeys().toArray());
                }
                broadcastManager.publish(m);
            }
        };
        cache.config().getMonitors().add(monitor);
    }
}
