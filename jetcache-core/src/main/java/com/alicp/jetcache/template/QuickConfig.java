/**
 * Created on 2022/07/30.
 */
package com.alicp.jetcache.template;

import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;

import java.time.Duration;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class QuickConfig {
    private String area = CacheConsts.DEFAULT_AREA;
    private String name;
    private Duration expire;
    private Duration localExpire;
    private Integer localLimit;
    private CacheType cacheType;
    private Boolean syncLocal;
    private Function<Object, Object> keyConvertor;
    private Function<Object, byte[]> valueEncoder;
    private Function<byte[], Object> valueDecoder;
    private Boolean cacheNullValue;
    private Boolean useAreaInPrefix;
    private Boolean penetrationProtect;
    private Duration penetrationProtectTimeout;
    private RefreshPolicy refreshPolicy;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Duration getExpire() {
        return expire;
    }

    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    public Duration getLocalExpire() {
        return localExpire;
    }

    public void setLocalExpire(Duration localExpire) {
        this.localExpire = localExpire;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public Integer getLocalLimit() {
        return localLimit;
    }

    public void setLocalLimit(Integer localLimit) {
        this.localLimit = localLimit;
    }

    public Boolean getSyncLocal() {
        return syncLocal;
    }

    public void setSyncLocal(Boolean syncLocal) {
        this.syncLocal = syncLocal;
    }

    public Function<Object, Object> getKeyConvertor() {
        return keyConvertor;
    }

    public void setKeyConvertor(Function<Object, Object> keyConvertor) {
        this.keyConvertor = keyConvertor;
    }

    public Function<Object, byte[]> getValueEncoder() {
        return valueEncoder;
    }

    public void setValueEncoder(Function<Object, byte[]> valueEncoder) {
        this.valueEncoder = valueEncoder;
    }

    public Function<byte[], Object> getValueDecoder() {
        return valueDecoder;
    }

    public void setValueDecoder(Function<byte[], Object> valueDecoder) {
        this.valueDecoder = valueDecoder;
    }

    public Boolean getCacheNullValue() {
        return cacheNullValue;
    }

    public void setCacheNullValue(Boolean cacheNullValue) {
        this.cacheNullValue = cacheNullValue;
    }

    public Boolean getUseAreaInPrefix() {
        return useAreaInPrefix;
    }

    public void setUseAreaInPrefix(Boolean useAreaInPrefix) {
        this.useAreaInPrefix = useAreaInPrefix;
    }

    public Boolean getPenetrationProtect() {
        return penetrationProtect;
    }

    public void setPenetrationProtect(Boolean penetrationProtect) {
        this.penetrationProtect = penetrationProtect;
    }

    public Duration getPenetrationProtectTimeout() {
        return penetrationProtectTimeout;
    }

    public void setPenetrationProtectTimeout(Duration penetrationProtectTimeout) {
        this.penetrationProtectTimeout = penetrationProtectTimeout;
    }

    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
    }
}
