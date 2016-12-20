package com.alicp.jetcache.embedded;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/20.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SimpleLock implements AutoReleaseLock {

    private Cache cache;
    private Object key;
    private long expireTimestamp;

    private SimpleLock(Cache cache, Object key, long expireTimestamp) {
        this.cache = cache;
        this.key = key;
        this.expireTimestamp = expireTimestamp;
    }

    public static SimpleLock tryLock(Cache cache, Object key, long expire, TimeUnit timeUnit) {
        long expireTimestamp = System.currentTimeMillis() + timeUnit.toMillis(expire);
        synchronized (cache) {
            SimpleLock fromCache = (SimpleLock) cache.get(key);
            if (fromCache == null) {
                SimpleLock lock = new SimpleLock(cache, key, expireTimestamp);
                cache.put(key, lock, expire, timeUnit);
                return lock;
            } else {
                return null;
            }
        }
    }

    @Override
    public void close() {
        long t = System.currentTimeMillis();
        if (t < expireTimestamp) {
            synchronized (cache) {
                if (cache.get(key) == this) {
                    cache.remove(key);
                }
            }
        }
    }
}
