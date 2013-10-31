/**
 * Created on  13-09-10 10:33
 */
package com.taobao.geek.jetcache.support;

import com.taobao.geek.jetcache.CacheConsts;
import com.taobao.geek.jetcache.CacheType;
import com.taobao.geek.jetcache.SerialPolicy;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfig {

    private String area = CacheConsts.DEFAULT_AREA;
    private boolean enabled = CacheConsts.DEFAULT_ENABLED;
    private int expire = CacheConsts.DEFAULT_EXPIRE;
    private CacheType cacheType = CacheConsts.DEFAULT_CACHE_TYPE;
    private int localLimit = CacheConsts.DEFAULT_LOCAL_LIMIT;
    private int version = CacheConsts.DEFAULT_VERSION;
    private boolean cacheNullValue = CacheConsts.DEFAULT_CACHE_NULL_VALUE;
    private String condition = CacheConsts.DEFAULT_CONDITION;
    private String unless = CacheConsts.DEFAULT_UNLESS;
    private SerialPolicy serialPolicy = CacheConsts.DEFAULT_SERIAL_POLICY;

    public String getArea() {
        return area;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getExpire() {
        return expire;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public int getLocalLimit() {
        return localLimit;
    }

    public int getVersion() {
        return version;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public void setLocalLimit(int localLimit) {
        this.localLimit = localLimit;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isCacheNullValue() {
        return cacheNullValue;
    }

    public void setCacheNullValue(boolean cacheNullValue) {
        this.cacheNullValue = cacheNullValue;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getUnless() {
        return unless;
    }

    public void setUnless(String unless) {
        this.unless = unless;
    }

    public SerialPolicy getSerialPolicy() {
        return serialPolicy;
    }

    public void setSerialPolicy(SerialPolicy serialPolicy) {
        this.serialPolicy = serialPolicy;
    }
}
