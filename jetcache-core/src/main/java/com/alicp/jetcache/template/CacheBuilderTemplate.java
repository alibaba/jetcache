/**
 * Created on 2022/07/30.
 */
package com.alicp.jetcache.template;

import com.alicp.jetcache.AbstractCacheBuilder;
import com.alicp.jetcache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author huangli
 */
public class CacheBuilderTemplate {
    private final boolean penetrationProtect;
    private final boolean useDefaultLocalExpireInMultiLevelCache;
    private final Map<String, CacheBuilder>[] cacheBuilders;
    private final List<CacheMonitorInstaller> cacheMonitorInstallers = new ArrayList<>();

    @SafeVarargs
    public CacheBuilderTemplate(boolean penetrationProtect, boolean useDefaultLocalExpireInMultiLevelCache, Map<String, CacheBuilder>... cacheBuilders) {
        this.penetrationProtect = penetrationProtect;
        this.useDefaultLocalExpireInMultiLevelCache = useDefaultLocalExpireInMultiLevelCache;
        this.cacheBuilders = cacheBuilders;
    }

    @SafeVarargs
    public CacheBuilderTemplate(boolean penetrationProtect, Map<String, CacheBuilder>... cacheBuilders) {
        this(penetrationProtect, false, cacheBuilders);
    }

    public boolean isPenetrationProtect() {
        return penetrationProtect;
    }

    public boolean isUseDefaultLocalExpireInMultiLevelCache() {
        return useDefaultLocalExpireInMultiLevelCache;
    }

    public CacheBuilder getCacheBuilder(int level, String area) {
        CacheBuilder cb = cacheBuilders[level].get(area);
        if (cb instanceof AbstractCacheBuilder) {
            return (CacheBuilder) ((AbstractCacheBuilder<?>) cb).clone();
        } else {
            return cb;
        }
    }

    public List<CacheMonitorInstaller> getCacheMonitorInstallers() {
        return cacheMonitorInstallers;
    }
}
