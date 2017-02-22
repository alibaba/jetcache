package com.alicp.jetcache.event;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;

/**
 * Created on 2017/2/22.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheGetEvent extends CacheEvent {

    private long millis;
    private Object key;
    private CacheGetResult result;

    public CacheGetEvent(Cache cache, long millis, Object key, CacheGetResult result) {
        super(cache);
        this.millis = millis;
        this.key = key;
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

    public CacheGetResult getResult() {
        return result;
    }

    public void setResult(CacheGetResult result) {
        this.result = result;
    }
}
