package com.alicp.jetcache.event;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheResult;

/**
 * Created on 2017/2/22.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheRemoveEvent extends CacheEvent {

    private long millis;
    private Object key;
    private CacheResult result;

    public CacheRemoveEvent(Cache cache, long millis, Object key, CacheResult result) {
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

    public CacheResult getResult() {
        return result;
    }

    public void setResult(CacheResult result) {
        this.result = result;
    }
}
