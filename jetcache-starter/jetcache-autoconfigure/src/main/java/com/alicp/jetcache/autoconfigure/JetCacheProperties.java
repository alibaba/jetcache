package com.alicp.jetcache.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@ConfigurationProperties(prefix = "jetcache")
public class JetCacheProperties {

    private String[] hiddenPackages;
    private int statIntervalMinutes;
    private boolean areaInCacheName = true;
    private boolean penetrationProtect = false;
    private boolean enableMethodCache = true;

    public JetCacheProperties(){
    }

    public String[] getHiddenPackages() {
        // keep same with GlobalCacheConfig
        return hiddenPackages;
    }

    public void setHiddenPackages(String[] hiddenPackages) {
        // keep same with GlobalCacheConfig
        this.hiddenPackages = hiddenPackages;
    }

    public void setHidePackages(String[] hidePackages) {
        // keep same with GlobalCacheConfig
        this.hiddenPackages = hidePackages;
    }

    public int getStatIntervalMinutes() {
        return statIntervalMinutes;
    }

    public void setStatIntervalMinutes(int statIntervalMinutes) {
        this.statIntervalMinutes = statIntervalMinutes;
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
