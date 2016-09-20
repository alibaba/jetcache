/**
 * Created on  13-09-10 14:42
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.cache.CacheManager;
import com.alicp.jetcache.cache.KeyGenerator;
import com.alicp.jetcache.impl.CacheImplSupport;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheProvider {

    //TODO fix it
    private CacheManager remoteCache = CacheManager.defaultInstance();
    private CacheManager localCache = CacheManager.defaultInstance();

    private KeyGenerator keyGenerator = CacheImplSupport.getDefaultKeyGenerator();

    public CacheManager getRemoteCache() {
        return remoteCache;
    }

    public void setRemoteCache(CacheManager remoteCache) {
        this.remoteCache = remoteCache;
    }

    public CacheManager getLocalCache() {
        return localCache;
    }

    public void setLocalCache(CacheManager localCache) {
        this.localCache = localCache;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }
}
