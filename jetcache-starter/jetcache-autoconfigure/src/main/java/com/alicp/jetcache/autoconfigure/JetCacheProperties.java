package com.alicp.jetcache.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created on 2016/11/23.
 *
 * @author huangli
 */
@ConfigurationProperties(prefix = "jetcache")
public class JetCacheProperties {

    private String[] hiddenPackages;
    private int statIntervalMinutes;
    private boolean areaInCacheName = false;
    private boolean penetrationProtect = false;
    private boolean enableMethodCache = true;
    private boolean useDefaultLocalExpireInMultiLevelCache = false;
    private boolean decodeFilterEnabled = true;
    private List<String> decodeFilterAllowPatterns;
    private List<String> decodeFilterDenyPatterns;

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

    public boolean isUseDefaultLocalExpireInMultiLevelCache() {
        return useDefaultLocalExpireInMultiLevelCache;
    }

    public void setUseDefaultLocalExpireInMultiLevelCache(boolean useDefaultLocalExpireInMultiLevelCache) {
        this.useDefaultLocalExpireInMultiLevelCache = useDefaultLocalExpireInMultiLevelCache;
    }

    public boolean isDecodeFilterEnabled() {
        return decodeFilterEnabled;
    }

    public void setDecodeFilterEnabled(boolean decodeFilterEnabled) {
        this.decodeFilterEnabled = decodeFilterEnabled;
    }

    public List<String> getDecodeFilterAllowPatterns() {
        return decodeFilterAllowPatterns;
    }

    public void setDecodeFilterAllowPatterns(List<String> decodeFilterAllowPatterns) {
        this.decodeFilterAllowPatterns = decodeFilterAllowPatterns;
    }

    public List<String> getDecodeFilterDenyPatterns() {
        return decodeFilterDenyPatterns;
    }

    public void setDecodeFilterDenyPatterns(List<String> decodeFilterDenyPatterns) {
        this.decodeFilterDenyPatterns = decodeFilterDenyPatterns;
    }

}
