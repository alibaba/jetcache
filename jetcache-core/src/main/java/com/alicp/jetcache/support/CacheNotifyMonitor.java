/**
 * Created on 2022-05-04.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheMonitor;
import com.alicp.jetcache.CacheUtil;
import com.alicp.jetcache.event.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheNotifyMonitor implements CacheMonitor {
    private final BroadcastManager broadcastManager;
    private final String area;
    private final String cacheName;
    private final Cache cache;

    public CacheNotifyMonitor(BroadcastManager broadcastManager, String area, String cacheName, Cache cache) {
        this.broadcastManager = broadcastManager;
        this.area = area;
        this.cacheName = cacheName;
        this.cache = cache;
    }

    @Override
    public void afterOperation(CacheEvent event) {
        AbstractCache absCache = CacheUtil.getAbstractCache(cache);
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
    }
}
