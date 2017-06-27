package com.alicp.jetcache.event;

import com.alicp.jetcache.Cache;

import java.util.Map;
import java.util.Set;

/**
 * Created on 2017/5/23.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheLoadAllEvent extends CacheEvent {

    private long millis;
    private Set keys;
    private Map loadedValue;
    private boolean success;

    public CacheLoadAllEvent(Cache cache, long millis, Set keys, Map loadedValue, boolean success) {
        super(cache);
        this.millis = millis;
        this.keys = keys;
        this.loadedValue = loadedValue;
        this.success = success;
    }

    public long getMillis() {
        return millis;
    }

    public Set getKeys() {
        return keys;
    }

    public Map getLoadedValue() {
        return loadedValue;
    }

    public boolean isSuccess() {
        return success;
    }
}
