/**
 * Created on  13-09-10 14:42
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.impl.CacheImplSupport;
import com.alicp.jetcache.local.LinkedHashMapCache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheProvider {

    private Cache remoteCache;
    private Cache localCache = new LinkedHashMapCache();
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
