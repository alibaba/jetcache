package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheConfig<K, V> implements Cloneable {
    private long expireAfterWriteInMillis = CacheConsts.DEFAULT_EXPIRE * 1000L;
    private long expireAfterAccessInMillis = 0;
    private Function<K, Object> keyConvertor;

    private CacheLoader<K, V> loader;

    private List<CacheMonitor> monitors = new ArrayList<>();

    private boolean cacheNullValue = false;

    private RefreshPolicy refreshPolicy;

    private int tryLockUnlockCount = 2;

    private int tryLockInquiryCount = 1;

    private int tryLockLockCount = 2;

    private boolean cachePenetrationProtect = false;

    @Override
    public CacheConfig clone() {
        try {
            CacheConfig copy = (CacheConfig) super.clone();
            if (monitors != null) {
                copy.monitors = new ArrayList(this.monitors);
            }
            if (refreshPolicy != null) {
                copy.refreshPolicy = this.refreshPolicy.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    public Function<K, Object> getKeyConvertor() {
        return keyConvertor;
    }

    public void setKeyConvertor(Function<K, Object> keyConvertor) {
        this.keyConvertor = keyConvertor;
    }

    public boolean isExpireAfterAccess() {
        return expireAfterAccessInMillis > 0;
    }

    public boolean isExpireAfterWrite() {
        return expireAfterWriteInMillis > 0;
    }

    @Deprecated
    public long getDefaultExpireInMillis() {
        return expireAfterWriteInMillis;
    }

    @Deprecated
    public void setDefaultExpireInMillis(long defaultExpireInMillis) {
        this.expireAfterWriteInMillis = defaultExpireInMillis;
    }

    public long getExpireAfterWriteInMillis() {
        return expireAfterWriteInMillis;
    }

    public void setExpireAfterWriteInMillis(long expireAfterWriteInMillis) {
        this.expireAfterWriteInMillis = expireAfterWriteInMillis;
    }

    public long getExpireAfterAccessInMillis() {
        return expireAfterAccessInMillis;
    }

    public void setExpireAfterAccessInMillis(long expireAfterAccessInMillis) {
        this.expireAfterAccessInMillis = expireAfterAccessInMillis;
    }

    public CacheLoader<K, V> getLoader() {
        return loader;
    }

    public void setLoader(CacheLoader<K, V> loader) {
        this.loader = loader;
    }

    public boolean isCacheNullValue() {
        return cacheNullValue;
    }

    public void setCacheNullValue(boolean cacheNullValue) {
        this.cacheNullValue = cacheNullValue;
    }

    public List<CacheMonitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<CacheMonitor> monitors) {
        this.monitors = monitors;
    }

    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
    }

    public int getTryLockUnlockCount() {
        return tryLockUnlockCount;
    }

    public void setTryLockUnlockCount(int tryLockUnlockCount) {
        this.tryLockUnlockCount = tryLockUnlockCount;
    }

    public int getTryLockInquiryCount() {
        return tryLockInquiryCount;
    }

    public void setTryLockInquiryCount(int tryLockInquiryCount) {
        this.tryLockInquiryCount = tryLockInquiryCount;
    }

    public int getTryLockLockCount() {
        return tryLockLockCount;
    }

    public void setTryLockLockCount(int tryLockLockCount) {
        this.tryLockLockCount = tryLockLockCount;
    }

    public boolean isCachePenetrationProtect() {
        return cachePenetrationProtect;
    }

    public void setCachePenetrationProtect(boolean cachePenetrationProtect) {
        this.cachePenetrationProtect = cachePenetrationProtect;
    }
}
