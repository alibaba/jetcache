/**
 * Created on  13-09-10 14:42
 */
package com.taobao.geek.jetcache;

import com.taobao.geek.jetcache.impl.CacheImplSupport;
import com.taobao.geek.jetcache.local.LRUMapCache;

/**
 * @author yeli.hl
 */
public class CacheProvider {

    private Cache remoteCache;
    private Cache localCache = new LRUMapCache();
    private KeyGenerator keyGenerator = CacheImplSupport.getDefaultKeyGenerator();

    public Cache getRemoteCache() {
        return remoteCache;
    }

    public void setRemoteCache(Cache remoteCache) {
        this.remoteCache = remoteCache;
    }

    public Cache getLocalCache() {
        return localCache;
    }

    public void setLocalCache(Cache localCache) {
        this.localCache = localCache;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }
}
