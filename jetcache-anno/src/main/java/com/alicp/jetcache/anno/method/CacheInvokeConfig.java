/**
 * Created on  13-09-21 23:04
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.support.CacheInvalidateAnnoConfig;
import com.alicp.jetcache.anno.support.CachedAnnoConfig;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheInvokeConfig {
    private CachedAnnoConfig cachedAnnoConfig;
    private CacheInvalidateAnnoConfig invalidateAnnoConfig;
    private boolean enableCacheContext;

    private Function<Object, Boolean> cachedConditionEvaluator;
    private Function<Object, Boolean> cachedUnlessEvaluator;
    private Function<Object, Object> cachedKeyEvaluator;

    private Function<Object, Boolean> invalidateConditionEvaluator;
    private Function<Object, Object> invalidateKeyEvaluator;

    private Cache cache;

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

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Function<Object, Boolean> getCachedConditionEvaluator() {
        return cachedConditionEvaluator;
    }

    public void setCachedConditionEvaluator(Function<Object, Boolean> cachedConditionEvaluator) {
        this.cachedConditionEvaluator = cachedConditionEvaluator;
    }

    public Function<Object, Boolean> getCachedUnlessEvaluator() {
        return cachedUnlessEvaluator;
    }

    public void setCachedUnlessEvaluator(Function<Object, Boolean> cachedUnlessEvaluator) {
        this.cachedUnlessEvaluator = cachedUnlessEvaluator;
    }

    public Function<Object, Object> getCachedKeyEvaluator() {
        return cachedKeyEvaluator;
    }

    public void setCachedKeyEvaluator(Function<Object, Object> cachedKeyEvaluator) {
        this.cachedKeyEvaluator = cachedKeyEvaluator;
    }

    public CacheInvalidateAnnoConfig getInvalidateAnnoConfig() {
        return invalidateAnnoConfig;
    }

    public void setInvalidateAnnoConfig(CacheInvalidateAnnoConfig invalidateAnnoConfig) {
        this.invalidateAnnoConfig = invalidateAnnoConfig;
    }

    public Function<Object, Boolean> getInvalidateConditionEvaluator() {
        return invalidateConditionEvaluator;
    }

    public void setInvalidateConditionEvaluator(Function<Object, Boolean> invalidateConditionEvaluator) {
        this.invalidateConditionEvaluator = invalidateConditionEvaluator;
    }

    public Function<Object, Object> getInvalidateKeyEvaluator() {
        return invalidateKeyEvaluator;
    }

    public void setInvalidateKeyEvaluator(Function<Object, Object> invalidateKeyEvaluator) {
        this.invalidateKeyEvaluator = invalidateKeyEvaluator;
    }
}
