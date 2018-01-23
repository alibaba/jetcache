/**
 * Created on  13-09-21 23:04
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.support.CacheInvalidateAnnoConfig;
import com.alicp.jetcache.anno.support.CacheUpdateAnnoConfig;
import com.alicp.jetcache.anno.support.CachedAnnoConfig;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheInvokeConfig {
    private CachedAnnoConfig cachedAnnoConfig;
    private CacheInvalidateAnnoConfig invalidateAnnoConfig;
    private CacheUpdateAnnoConfig updateAnnoConfig;
    private boolean enableCacheContext;

    private static final CacheInvokeConfig noCacheInvokeConfigInstance = new CacheInvokeConfig();

    public static CacheInvokeConfig getNoCacheInvokeConfigInstance() {
        return noCacheInvokeConfigInstance;
    }

    public CachedAnnoConfig getCachedAnnoConfig() {
        return cachedAnnoConfig;
    }

    public void setCachedAnnoConfig(CachedAnnoConfig cachedAnnoConfig) {
        this.cachedAnnoConfig = cachedAnnoConfig;
    }

    public boolean isEnableCacheContext() {
        return enableCacheContext;
    }

    public void setEnableCacheContext(boolean enableCacheContext) {
        this.enableCacheContext = enableCacheContext;
    }

    public CacheInvalidateAnnoConfig getInvalidateAnnoConfig() {
        return invalidateAnnoConfig;
    }

    public void setInvalidateAnnoConfig(CacheInvalidateAnnoConfig invalidateAnnoConfig) {
        this.invalidateAnnoConfig = invalidateAnnoConfig;
    }

    public CacheUpdateAnnoConfig getUpdateAnnoConfig() {
        return updateAnnoConfig;
    }

    public void setUpdateAnnoConfig(CacheUpdateAnnoConfig updateAnnoConfig) {
        this.updateAnnoConfig = updateAnnoConfig;
    }
}
