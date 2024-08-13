/**
 * Created on 2017/2/22.
 */
package com.alicp.jetcache.event;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.support.Epoch;

/**
 * The CacheEvent is used in single JVM while CacheMessage used for distributed message.
 *
 * @author huangli
 */
public class CacheEvent {

    private final long epoch = Epoch.get();

    protected Cache cache;

    public CacheEvent(Cache cache) {
        this.cache = cache;
    }

    public Cache getCache() {
        return cache;
    }

    public long getEpoch() {
        return epoch;
    }
}
