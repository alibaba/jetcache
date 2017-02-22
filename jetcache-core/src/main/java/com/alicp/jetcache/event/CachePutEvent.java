package com.alicp.jetcache.event;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheResult;

/**
 * Created on 2017/2/22.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CachePutEvent extends CacheEvent {
    private long millis;
    private Object key;
    private Object value;
    private CacheResult result;

    public CachePutEvent(Cache cache, long millis, Object key, Object value, CacheResult result) {
        super(cache);
        this.millis = millis;
        this.key = key;
        this.value = value;
        this.result = result;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public CacheResult getResult() {
        return result;
    }

    public void setResult(CacheResult result) {
        this.result = result;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
