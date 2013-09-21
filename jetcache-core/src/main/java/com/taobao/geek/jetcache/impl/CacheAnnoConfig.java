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
    private boolean enableCache;

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

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }
}
