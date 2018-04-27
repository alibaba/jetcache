package com.alicp.jetcache.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@ConfigurationProperties(prefix = "jetcache")
public class JetCacheProperties {

    private String[] hidePackages;
    private int statIntervalMinutes;
    private boolean areaInCacheName = true;
    private boolean penetrationProtect = false;

    public JetCacheProperties(){
    }

    public String[] getHidePackages() {
        return hidePackages;
    }

    public void setHidePackages(String[] hidePackages) {
        this.hidePackages = hidePackages;
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
}
