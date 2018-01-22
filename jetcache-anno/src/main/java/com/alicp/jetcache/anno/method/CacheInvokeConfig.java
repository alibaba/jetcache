/**
 * Created on  13-09-21 23:04
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.support.CachedAnnoConfig;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheInvokeConfig {
    private CachedAnnoConfig cachedAnnoConfig;
    private boolean enableCacheContext;

    private Function<Object, Boolean> conditionEvaluator;
    private Function<Object, Boolean> unlessEvaluator;
    private Function<Object, Object> keyEvaluator;

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

    public Function<Object, Boolean> getConditionEvaluator() {
        return conditionEvaluator;
    }

    public void setConditionEvaluator(Function<Object, Boolean> conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    public Function<Object, Boolean> getUnlessEvaluator() {
        return unlessEvaluator;
    }

    public void setUnlessEvaluator(Function<Object, Boolean> unlessEvaluator) {
        this.unlessEvaluator = unlessEvaluator;
    }

    public Function<Object, Object> getKeyEvaluator() {
        return keyEvaluator;
    }

    public void setKeyEvaluator(Function<Object, Object> keyEvaluator) {
        this.keyEvaluator = keyEvaluator;
    }
}
