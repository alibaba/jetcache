package com.alicp.jetcache.event;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MultiGetResult;

import java.util.Set;

/**
 * Created on 2017/2/22.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheGetAllEvent extends CacheEvent {
    private final long millis;
    private final Set keys;
    private final MultiGetResult result;

    public CacheGetAllEvent(Cache cache, long millis, Set keys, MultiGetResult result) {
        super(cache);
        this.millis = millis;
        this.keys = keys;
        this.result = result;
    }

    public long getMillis() {
        return millis;
    }

    public Set getKeys() {
        return keys;
    }

    public MultiGetResult getResult() {
        return result;
    }
}
