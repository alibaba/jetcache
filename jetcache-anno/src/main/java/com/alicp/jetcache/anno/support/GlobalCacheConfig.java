/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheBuilder;

import java.util.Map;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class GlobalCacheConfig {

    private String[] hiddenPackages;
    protected int statIntervalMinutes;
    /**
     * for compatible reason. This property controls whether add area as remote cache key prefix.
     * version<=2.4.3: add cache area in prefix, no config.
     * version>2.4.3 and version <2.7: default value is true, keep same as 2.4.3 if not set.
     * version>=2.7.0.RC: default value is false.
     *
     * remove in the future.
     */
    @Deprecated
    private boolean areaInCacheName = false;
    private boolean penetrationProtect = false;
    private boolean enableMethodCache = true;

    private Map<String, CacheBuilder> localCacheBuilders;
    private Map<String, CacheBuilder> remoteCacheBuilders;

    public GlobalCacheConfig() {
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

    /**
     * for compatible reason. This property controls whether add area as remote cache key prefix.
     * version<=2.4.3: add cache area in prefix, no config.
     * version>2.4.3 and version <2.7: default value is true, keep same as 2.4.3 if not set.
     * version>=2.7.0.RC: default value is false.
     *
     * remove in the future.
     */
    @Deprecated
    public boolean isAreaInCacheName() {
        return areaInCacheName;
    }

    /**
     * for compatible reason. This property controls whether add area as remote cache key prefix.
     * version<=2.4.3: add cache area in prefix, no config.
     * version>2.4.3 and version <2.7: default value is true, keep same as 2.4.3 if not set.
     * version>=2.7.0.RC: default value is false.
     *
     * remove in the future.
     */
    @Deprecated
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
