/**
 * Created on  13-09-10 14:42
 */
package com.taobao.geek.cache;

/**
 * @author yeli.hl
 */
public class CacheProvider {

    private Cache remoteCache;
    private Cache localCache;
    private KeyGenerator keyGenerator = new DefaultKeyGenerator();

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
