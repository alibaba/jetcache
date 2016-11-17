/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.factory.CacheFactory;

import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class GlobalCacheConfig {

    private String[] hidePackages;
    private Map<String, CacheBuilder> localCacheBuilders;
    private Map<String, CacheBuilder> remoteCacheBuilders;
    private CacheContext cacheContext;

    public GlobalCacheConfig() {
    }

    public CacheContext cacheContext(){
        if (cacheContext != null) {
            return cacheContext;
        }
        synchronized (this) {
            cacheContext = newCacheContext();
            cacheContext.init();
        }
        return cacheContext;
    }

    protected CacheContext newCacheContext(){
        return new CacheContext(this);
    }

    public String[] getHidePackages() {
        return hidePackages;
    }

    public void setHidePackages(String[] hidePackages) {
        this.hidePackages = hidePackages;
    }

    public Map<String, CacheBuilder> getLocalCacheBuilders() {
        return localCacheBuilders;
    }

    public void setLocalCacheBuilders(Map<String, CacheBuilder> localCacheBuilders) {
        this.localCacheBuilders = localCacheBuilders;
    }

    public Map<String, CacheBuilder> getRemoteCacheBuilders() {
        return remoteCacheBuilders;
    }

    public void setRemoteCacheBuilders(Map<String, CacheBuilder> remoteCacheBuilders) {
        this.remoteCacheBuilders = remoteCacheBuilders;
    }

}
