package com.alicp.jetcache;

import java.io.Serializable;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheStat implements Serializable, Cloneable {

    private static final long serialVersionUID = -8802969946750554026L;

    protected String cacheName;
    protected long statStartTime;
    protected long statEndTime;

    protected long getCount;
    protected long getHitCount;
    protected long getMissCount;
    protected long getFailCount;
    protected long getExpireCount;
    protected long getTimeSum;
    protected long minGetTime = Long.MAX_VALUE;
    protected long maxGetTime = Long.MIN_VALUE;

    protected long putCount;
    protected long putSuccessCount;
    protected long putFailCount;
    protected long putTimeSum;
    protected long minPutTime = Long.MAX_VALUE;
    protected long maxPutTime = Long.MIN_VALUE;

    protected long invalidateCount;
    protected long invalidateSuccessCount;
    protected long invalidateFailCount;
    protected long invalidateTimeSum;
    protected long minInvalidateTime = Long.MAX_VALUE;
    protected long maxInvalidateTime = Long.MIN_VALUE;

    protected long loadCount;
    protected long loadSuccessCount;
    protected long loadFailCount;
    protected long loadTimeSum;
    protected long minLoadTime = Long.MAX_VALUE;
    protected long maxLoadTime = Long.MIN_VALUE;

    @Override
    public CacheStat clone() {
        try {
            return (CacheStat) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    public double hitRate() {
        if (getCount == 0) {
            return 0;
        }
        return 1.0 * getHitCount / getCount;
    }

    public double avgGetTime() {
        if (getCount == 0) {
            return 0;
        }
        return 1.0 * getTimeSum / getCount;
    }

    public double avgPutTime() {
        if (putCount == 0) {
            return 0;
        }
        return 1.0 * putTimeSum / putCount;
    }

    public double avgInvalidateTime() {
        if (invalidateCount == 0) {
            return 0;
        }
        return 1.0 * invalidateTimeSum / invalidateCount;
    }

    public double avgLoadTime() {
        if (loadCount == 0) {
            return 0;
        }
        return 1.0 * loadTimeSum / loadCount;
    }

    //---------------------------------------------------------------------


    public long getGetCount() {
        return getCount;
    }

    public void setGetCount(long getCount) {
        this.getCount = getCount;
    }

    public long getGetHitCount() {
        return getHitCount;
    }

    public void setGetHitCount(long getHitCount) {
        this.getHitCount = getHitCount;
    }

    public long getGetMissCount() {
        return getMissCount;
    }

    public void setGetMissCount(long getMissCount) {
        this.getMissCount = getMissCount;
    }

    public long getGetFailCount() {
        return getFailCount;
    }

    public void setGetFailCount(long getFailCount) {
        this.getFailCount = getFailCount;
    }

    public long getGetExpireCount() {
        return getExpireCount;
    }

    public void setGetExpireCount(long getExpireCount) {
        this.getExpireCount = getExpireCount;
    }

    public long getGetTimeSum() {
        return getTimeSum;
    }

    public void setGetTimeSum(long getTimeSum) {
        this.getTimeSum = getTimeSum;
    }

    public long getMinGetTime() {
        return minGetTime;
    }

    public void setMinGetTime(long minGetTime) {
        this.minGetTime = minGetTime;
    }

    public long getMaxGetTime() {
        return maxGetTime;
    }

    public void setMaxGetTime(long maxGetTime) {
        this.maxGetTime = maxGetTime;
    }

    public long getPutCount() {
        return putCount;
    }

    public void setPutCount(long putCount) {
        this.putCount = putCount;
    }

    public long getPutSuccessCount() {
        return putSuccessCount;
    }

    public void setPutSuccessCount(long putSuccessCount) {
        this.putSuccessCount = putSuccessCount;
    }

    public long getPutFailCount() {
        return putFailCount;
    }

    public void setPutFailCount(long putFailCount) {
        this.putFailCount = putFailCount;
    }

    public long getPutTimeSum() {
        return putTimeSum;
    }

    public void setPutTimeSum(long putTimeSum) {
        this.putTimeSum = putTimeSum;
    }

    public long getMinPutTime() {
        return minPutTime;
    }

    public void setMinPutTime(long minPutTime) {
        this.minPutTime = minPutTime;
    }

    public long getMaxPutTime() {
        return maxPutTime;
    }

    public void setMaxPutTime(long maxPutTime) {
        this.maxPutTime = maxPutTime;
    }

    public long getInvalidateCount() {
        return invalidateCount;
    }

    public void setInvalidateCount(long invalidateCount) {
        this.invalidateCount = invalidateCount;
    }

    public long getInvalidateSuccessCount() {
        return invalidateSuccessCount;
    }

    public void setInvalidateSuccessCount(long invalidateSuccessCount) {
        this.invalidateSuccessCount = invalidateSuccessCount;
    }

    public long getInvalidateFailCount() {
        return invalidateFailCount;
    }

    public void setInvalidateFailCount(long invalidateFailCount) {
        this.invalidateFailCount = invalidateFailCount;
    }

    public long getInvalidateTimeSum() {
        return invalidateTimeSum;
    }

    public void setInvalidateTimeSum(long invalidateTimeSum) {
        this.invalidateTimeSum = invalidateTimeSum;
    }

    public long getMinInvalidateTime() {
        return minInvalidateTime;
    }

    public void setMinInvalidateTime(long minInvalidateTime) {
        this.minInvalidateTime = minInvalidateTime;
    }

    public long getMaxInvalidateTime() {
        return maxInvalidateTime;
    }

    public void setMaxInvalidateTime(long maxInvalidateTime) {
        this.maxInvalidateTime = maxInvalidateTime;
    }

    public long getLoadCount() {
        return loadCount;
    }

    public void setLoadCount(long loadCount) {
        this.loadCount = loadCount;
    }

    public long getLoadSuccessCount() {
        return loadSuccessCount;
    }

    public void setLoadSuccessCount(long loadSuccessCount) {
        this.loadSuccessCount = loadSuccessCount;
    }

    public long getLoadFailCount() {
        return loadFailCount;
    }

    public void setLoadFailCount(long loadFailCount) {
        this.loadFailCount = loadFailCount;
    }

    public long getLoadTimeSum() {
        return loadTimeSum;
    }

    public void setLoadTimeSum(long loadTimeSum) {
        this.loadTimeSum = loadTimeSum;
    }

    public long getMinLoadTime() {
        return minLoadTime;
    }

    public void setMinLoadTime(long minLoadTime) {
        this.minLoadTime = minLoadTime;
    }

    public long getMaxLoadTime() {
        return maxLoadTime;
    }

    public void setMaxLoadTime(long maxLoadTime) {
        this.maxLoadTime = maxLoadTime;
    }

    public long getStatStartTime() {
        return statStartTime;
    }

    public void setStatStartTime(long statStartTime) {
        this.statStartTime = statStartTime;
    }

    public long getStatEndTime() {
        return statEndTime;
    }

    public void setStatEndTime(long statEndTime) {
        this.statEndTime = statEndTime;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }
}
