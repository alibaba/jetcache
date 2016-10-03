package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;

import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfig implements Cloneable {
    private boolean cacheNullValue;
    private long defaultExpireInMillis = CacheConsts.DEFAULT_EXPIRE * 1000;
    private boolean expireAfterAccess = false;
    private Function<Object,Object> keyGenerator;

    @Override
    public CacheConfig clone() {
        try {
            return (CacheConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    public boolean isCacheNullValue() {
        return cacheNullValue;
    }

    public void setCacheNullValue(boolean cacheNullValue) {
        this.cacheNullValue = cacheNullValue;
    }

    public Function<Object,Object> getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(Function<Object,Object> keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public boolean isExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(boolean expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public long getDefaultExpireInMillis() {
        return defaultExpireInMillis;
    }

    public void setDefaultExpireInMillis(long defaultExpireInMillis) {
        this.defaultExpireInMillis = defaultExpireInMillis;
    }
}
