package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheMonitor;
import com.alicp.jetcache.CacheResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultCacheMonitor implements CacheMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheMonitor.class);

    protected CacheStat cacheStat;
    private String cacheName;

    public DefaultCacheMonitor(String cacheName) {
        this.cacheName = cacheName;
        resetStat();
    }

    public String getCacheName() {
        return cacheName;
    }

    public void resetStat() {
        cacheStat = new CacheStat();
        cacheStat.setStatStartTime(System.currentTimeMillis());
        cacheStat.setCacheName(cacheName);
    }

    public synchronized CacheStat getCacheStat() {
        CacheStat stat = cacheStat.clone();
        stat.setStatEndTime(System.currentTimeMillis());
        return stat;
    }

    @Override
    public synchronized void afterGET(long millis, Object key, CacheGetResult result) {
        cacheStat.minGetTime = Math.min(cacheStat.minGetTime, millis);
        cacheStat.maxGetTime = Math.max(cacheStat.maxGetTime, millis);
        cacheStat.getTimeSum += millis;
        cacheStat.getCount++;
        switch (result.getResultCode()) {
            case SUCCESS:
                cacheStat.getHitCount++;
                break;
            case NOT_EXISTS:
                cacheStat.getMissCount++;
                break;
            case EXPIRED:
                cacheStat.getExpireCount++;
                break;
            case FAIL:
                cacheStat.getFailCount++;
                break;
        }
    }

    @Override
    public synchronized void afterPUT(long millis, Object key, Object value, CacheResult result) {
        cacheStat.minPutTime = Math.min(cacheStat.minPutTime, millis);
        cacheStat.maxPutTime = Math.max(cacheStat.maxPutTime, millis);
        cacheStat.putTimeSum += millis;
        cacheStat.putCount++;
        switch (result.getResultCode()) {
            case SUCCESS:
                cacheStat.putSuccessCount++;
                break;
            case FAIL:
                cacheStat.putFailCount++;
                break;
            default:
                logger.warn("jetcache PUT return unexpected code: " + result.getResultCode());
        }
    }

    @Override
    public synchronized void afterINVALIDATE(long millis, Object key, CacheResult result) {
        cacheStat.minInvalidateTime = Math.min(cacheStat.minInvalidateTime, millis);
        cacheStat.maxInvalidateTime = Math.max(cacheStat.maxInvalidateTime, millis);
        cacheStat.invalidateTimeSum += millis;
        cacheStat.invalidateCount++;
        switch (result.getResultCode()) {
            case SUCCESS:
                cacheStat.invalidateSuccessCount++;
                break;
            case FAIL:
                cacheStat.invalidateFailCount++;
                break;
            default:
                logger.warn("jetcache INVALIDATE return unexpected code: " + result.getResultCode());
        }
    }

    @Override
    public synchronized void afterLoad(long millis, Object key, Object loadedValue, boolean success) {
        cacheStat.minLoadTime = Math.min(cacheStat.minLoadTime, millis);
        cacheStat.maxLoadTime = Math.max(cacheStat.maxLoadTime, millis);
        cacheStat.loadTimeSum += millis;
        cacheStat.loadCount++;
        if (success) {
            cacheStat.loadSuccessCount++;
        } else {
            cacheStat.loadFailCount++;
        }
    }
}
