/**
 * Created on  13-09-21 23:04
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInvokeConfig {
    CacheConfig cacheConfig;
    boolean enableCacheContext;

    EL conditionEL;
    EL unlessEL;

    private static final CacheInvokeConfig noCacheInvokeConfigInstance = new CacheInvokeConfig();

    public static CacheInvokeConfig getNoCacheInvokeConfigInstance() {
        return noCacheInvokeConfigInstance;
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

    public EL getConditionEL() {
        return conditionEL;
    }

    public void setConditionEL(EL conditionEL) {
        this.conditionEL = conditionEL;
    }

    public EL getUnlessEL() {
        return unlessEL;
    }

    public void setUnlessEL(EL unlessEL) {
        this.unlessEL = unlessEL;
    }
}
