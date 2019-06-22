/**
 * Created on 2017/2/22.
 */
package com.alicp.jetcache.event;

import com.alicp.jetcache.Cache;

/**
 * The CacheEvent is used in single JVM while CacheMessage used for distributed message.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheEvent {

    protected Cache cache;

    public CacheEvent(Cache cache) {
        this.cache = cache;
    }

    public Cache getCache() {
        return cache;
    }

}
