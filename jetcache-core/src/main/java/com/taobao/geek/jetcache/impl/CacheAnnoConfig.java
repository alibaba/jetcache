/**
 * Created on  13-09-21 23:04
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;

/**
 * @author yeli.hl
 */
public class CacheAnnoConfig {
    private CacheConfig cacheConfig;
    private boolean enableCacheContext;

    private static final CacheAnnoConfig noCacheAnnoConfigInstance = new CacheAnnoConfig();

    public static CacheAnnoConfig getNoCacheAnnoConfigInstance() {
        return noCacheAnnoConfigInstance;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    public boolean isEnableCacheContext() {
        return enableCacheContext;
    }

    public void setEnableCacheContext(boolean enableCacheContext) {
        this.enableCacheContext = enableCacheContext;
    }
}
