package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheException;

import java.io.Serializable;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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
    protected long maxGetTime = 0;

    protected long putCount;
    protected long putSuccessCount;
    protected long putFailCount;
    protected long putTimeSum;
    protected long minPutTime = Long.MAX_VALUE;
    protected long maxPutTime = 0;

    protected long removeCount;
    protected long removeSuccessCount;
    protected long removeFailCount;
    protected long removeTimeSum;
    protected long minRemoveTime = Long.MAX_VALUE;
    protected long maxRemoveTime = 0;

    protected long loadCount;
    protected long loadSuccessCount;
    protected long loadFailCount;
    protected long loadTimeSum;
    protected long minLoadTime = Long.MAX_VALUE;
    protected long maxLoadTime = 0;

    @Override
    public CacheStat clone() {
        try {
            return (CacheStat) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    private double tps(long count){
        long t = statEndTime;
        if (t == 0) {
            t = System.currentTimeMillis();
        }
        t = t - statStartTime;
        if (t == 0) {
            return 0;
        } else {
            return 1000.0 * count / t;
        }
    }

    public double qps() {
        return tps(getCount);
    }

    public double putTps() {
        return tps(putCount);
    }

    public double removeTps() {
        return tps(removeCount);
    }

    public double loadQps() {
        return tps(loadCount);
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

    public double avgRemoveTime() {
        if (removeCount == 0) {
            return 0;
        }
        return 1.0 * removeTimeSum / removeCount;
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

    public long getRemoveCount() {
        return removeCount;
    }

    public void setRemoveCount(long removeCount) {
        this.removeCount = removeCount;
    }

    public long getRemoveSuccessCount() {
        return removeSuccessCount;
    }

    public void setRemoveSuccessCount(long removeSuccessCount) {
        this.removeSuccessCount = removeSuccessCount;
    }

    public long getRemoveFailCount() {
        return removeFailCount;
    }

    public void setRemoveFailCount(long removeFailCount) {
        this.removeFailCount = removeFailCount;
    }

    public long getRemoveTimeSum() {
        return removeTimeSum;
    }

    public void setRemoveTimeSum(long removeTimeSum) {
        this.removeTimeSum = removeTimeSum;
    }

    public long getMinRemoveTime() {
        return minRemoveTime;
    }

    public void setMinRemoveTime(long minRemoveTime) {
        this.minRemoveTime = minRemoveTime;
    }

    public long getMaxRemoveTime() {
        return maxRemoveTime;
    }

    public void setMaxRemoveTime(long maxRemoveTime) {
        this.maxRemoveTime = maxRemoveTime;
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
