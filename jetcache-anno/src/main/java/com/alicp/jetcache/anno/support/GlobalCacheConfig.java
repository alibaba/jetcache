/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.factory.CacheFactory;

import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class GlobalCacheConfig {

    private String[] hidePackages;
    private Map<String, CacheFactory> localCacheFacotories;
    private Map<String, CacheFactory> remoteCacheFacotories;
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

    public Map<String, CacheFactory> getLocalCacheFacotories() {
        return localCacheFacotories;
    }

    public void setLocalCacheFacotories(Map<String, CacheFactory> localCacheFacotories) {
        this.localCacheFacotories = localCacheFacotories;
    }

    public Map<String, CacheFactory> getRemoteCacheFacotories() {
        return remoteCacheFacotories;
    }

    public void setRemoteCacheFacotories(Map<String, CacheFactory> remoteCacheFacotories) {
        this.remoteCacheFacotories = remoteCacheFacotories;
    }

}
