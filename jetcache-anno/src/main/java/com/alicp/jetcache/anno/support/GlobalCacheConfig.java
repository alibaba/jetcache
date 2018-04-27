/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class GlobalCacheConfig {

    private String[] hiddenPackages;
    protected int statIntervalMinutes;
    private boolean areaInCacheName = true;
    private boolean penetrationProtect = false;

    private Map<String, CacheBuilder> localCacheBuilders;
    private Map<String, CacheBuilder> remoteCacheBuilders;

    private ConfigProvider configProvider = new SpringConfigProvider();

    private CacheContext cacheContext;

    public GlobalCacheConfig() {
    }

    @PostConstruct
    public synchronized void init() {
        if (cacheContext == null) {
            cacheContext = configProvider.newContext(this);
            cacheContext.init();
        }
    }

    @PreDestroy
    public synchronized void shutdown() {
        if (cacheContext != null) {
            cacheContext.shutdown();
            cacheContext = null;
        }
    }

    public CacheContext getCacheContext() {
        return cacheContext;
    }

    public String[] getHiddenPackages() {
        return hiddenPackages;
    }

    public void setHiddenPackages(String[] hiddenPackages) {
        this.hiddenPackages = hiddenPackages;
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

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public boolean isAreaInCacheName() {
        return areaInCacheName;
    }

    public void setAreaInCacheName(boolean areaInCacheName) {
        this.areaInCacheName = areaInCacheName;
    }

    public boolean isPenetrationProtect() {
        return penetrationProtect;
    }

    public void setPenetrationProtect(boolean penetrationProtect) {
        this.penetrationProtect = penetrationProtect;
    }
}
