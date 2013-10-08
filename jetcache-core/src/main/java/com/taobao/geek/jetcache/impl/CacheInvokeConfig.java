/**
 * Created on  13-09-21 23:04
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.support.CacheConfig;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInvokeConfig {
    CacheConfig cacheConfig;
    boolean enableCacheContext;

    EL conditionEL;
    String conditionScript;
    EL unlessEL;
    String unlessScript;

    private static final CacheInvokeConfig noCacheInvokeConfigInstance = new CacheInvokeConfig();

    public static CacheInvokeConfig getNoCacheInvokeConfigInstance() {
        return noCacheInvokeConfigInstance;
    }

    public void init() {
        Object[] tmp = ExpressionUtil.parseEL(cacheConfig.getCondition());
        if (tmp != null) {
            conditionEL = (EL) tmp[0];
            conditionScript = (String) tmp[1];
        } else {
            conditionEL = null;
            conditionScript = null;
        }
        tmp = ExpressionUtil.parseEL(cacheConfig.getUnless());
        if (tmp != null) {
            unlessEL = (EL) tmp[0];
            unlessScript = (String) tmp[1];
        } else {
            unlessEL = null;
            unlessScript = null;
        }
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
