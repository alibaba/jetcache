/**
 * Created on  13-09-10 10:33
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.CacheType;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheAnnoConfig {

    private String area;
    private String name;
    private boolean enabled;
    private int expire;
    private CacheType cacheType;
    private int localLimit;
    private boolean cacheNullValue;
    private String condition;
    private String unless;
    private String serialPolicy;
    private String keyConvertor;

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

    public String getSerialPolicy() {
        return serialPolicy;
    }

    public void setSerialPolicy(String serialPolicy) {
        this.serialPolicy = serialPolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyConvertor() {
        return keyConvertor;
    }

    public void setKeyConvertor(String keyConvertor) {
        this.keyConvertor = keyConvertor;
    }
}
