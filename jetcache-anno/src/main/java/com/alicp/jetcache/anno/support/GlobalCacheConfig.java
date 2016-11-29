/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class GlobalCacheConfig {

    private String[] hidePackages;
    private Map<String, CacheBuilder> localCacheBuilders;
    private Map<String, CacheBuilder> remoteCacheBuilders;
    private CacheContext cacheContext;

    protected int statIntervalMinutes;
    protected Consumer<DefaultCacheMonitorManager.StatInfo> statCallback;

    public GlobalCacheConfig() {
    }

    public CacheContext cacheContext(){
        if (cacheContext != null) {
            return cacheContext;
        }
        synchronized (this) {
            if (cacheContext != null) {
                return cacheContext;
            }
            cacheContext = newCacheContext();
            cacheContext.init();
        }
        return cacheContext;
    }

    protected CacheContext newCacheContext(){
        return new CacheContext(this, statIntervalMinutes, statCallback);
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

    public int getStatIntervalMinutes() {
        return statIntervalMinutes;
    }

    public void setStatIntervalMinutes(int statIntervalMinutes) {
        this.statIntervalMinutes = statIntervalMinutes;
    }

    public Consumer<DefaultCacheMonitorManager.StatInfo> getStatCallback() {
        return statCallback;
    }

    public void setStatCallback(Consumer<DefaultCacheMonitorManager.StatInfo> statCallback) {
        this.statCallback = statCallback;
    }
}
