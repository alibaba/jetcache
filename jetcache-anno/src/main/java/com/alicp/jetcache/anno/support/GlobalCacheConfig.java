/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class GlobalCacheConfig implements InitializingBean, DisposableBean {

    private String[] hiddenPackages;
    protected int statIntervalMinutes;
    private boolean areaInCacheName = true;
    private boolean penetrationProtect = false;
    private boolean enableMethodCache = true;

    private Map<String, CacheBuilder> localCacheBuilders;
    private Map<String, CacheBuilder> remoteCacheBuilders;

    private ConfigProvider configProvider = new SpringConfigProvider();

    private CacheContext cacheContext;

    public GlobalCacheConfig() {
    }

    public void init() {
        afterPropertiesSet();
    }

    @Override
    public synchronized void afterPropertiesSet() {
        if (cacheContext == null) {
            cacheContext = configProvider.newContext(this);
            cacheContext.init();
        }
    }

    public void shutdown() {
        destroy();
    }

    @Override
    public synchronized void destroy() {
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

    public boolean isEnableMethodCache() {
        return enableMethodCache;
    }

    public void setEnableMethodCache(boolean enableMethodCache) {
        this.enableMethodCache = enableMethodCache;
    }
}
