/**
 * Created on  13-09-10 10:33
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.KeyConvertor;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheAnnoConfig {

    private String area = CacheConsts.DEFAULT_AREA;
    private String name = CacheConsts.DEFAULT_NAME;
    private boolean enabled = CacheConsts.DEFAULT_ENABLED;
    private int expire = CacheConsts.DEFAULT_EXPIRE;
    private CacheType cacheType = CacheConsts.DEFAULT_CACHE_TYPE;
    private int localLimit = CacheConsts.DEFAULT_LOCAL_LIMIT;
    private int version = CacheConsts.DEFAULT_VERSION;
    private boolean cacheNullValue = CacheConsts.DEFAULT_CACHE_NULL_VALUE;
    private String condition = CacheConsts.DEFAULT_CONDITION;
    private String unless = CacheConsts.DEFAULT_UNLESS;
    private String serialPolicy = CacheConsts.DEFAULT_SERIAL_POLICY;
    private String keyConvertor = KeyConvertor.FASTJSON;

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
