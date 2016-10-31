package com.alicp.jetcache;

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
    protected static ScheduledExecutorService executorService;

    protected CacheStat cacheStat;
    private String cacheName;

    public DefaultCacheMonitor(String cacheName) {
        this.cacheName = cacheName;
        initStat(cacheName);
    }

    public DefaultCacheMonitor(String cacheName, int resetTime, TimeUnit resetTimeUnit, Consumer<CacheStat> resetAction) {
        this.cacheName = cacheName;
        initStat(cacheName);
        if (executorService == null) {
            initExecutor();
        }
        Runnable cmd = () -> {
            CacheStat copy;
            synchronized (this) {
                copy = getCacheStat();
                initStat(cacheName);
            }
            if (resetAction != null) {
                resetAction.accept(copy);
            }
        };
        long delay = firstDelay(resetTime, resetTimeUnit);
        executorService.scheduleAtFixedRate(cmd, delay, resetTime, resetTimeUnit);
    }

    private void initStat(String cacheName) {
        cacheStat = new CacheStat();
        cacheStat.setStatStartTime(System.currentTimeMillis());
        cacheStat.setCacheName(cacheName);
    }

    protected long firstDelay(int resetTime, TimeUnit resetTimeUnit) {
        LocalDateTime firstResetTime = computeFirstResetTime(resetTime, resetTimeUnit);
        long d = firstResetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();
        return d;
    }

    protected LocalDateTime computeFirstResetTime(int time, TimeUnit unit) {
        if (unit != TimeUnit.SECONDS || unit != TimeUnit.MINUTES || unit != TimeUnit.HOURS || unit != TimeUnit.DAYS) {
            throw new IllegalArgumentException();
        }
        LocalDateTime t = LocalDateTime.now();
        switch (unit) {
            case DAYS:
                t = t.plusDays(1).withHour(0);
                t = t.plusHours(1).withMinute(0);
                t = t.plusMinutes(1).withSecond(0);
                t = t.plusSeconds(1).withNano(0);
                break;
            case HOURS:
                t = t.plusHours(1).withMinute(0);
                t = t.plusMinutes(1).withSecond(0);
                t = t.plusSeconds(1).withNano(0);
                if (24 % time == 0) {
                    t = t.plusHours(time - t.getHour() % time);
                }
                break;
            case MINUTES:
                t = t.plusMinutes(1).withSecond(0);
                t = t.plusSeconds(1).withNano(0);
                if (60 % time == 0) {
                    t = t.plusMinutes(time - t.getMinute() % time);
                }
                break;
            case SECONDS:
                t = t.plusSeconds(1).withNano(0);
                if (60 % time == 0) {
                    t = t.plusSeconds(time - t.getSecond() % time);
                }
                break;
        }
        return t;
    }

    public static ScheduledExecutorService executorService() {
        if (executorService != null) {
            return executorService;
        }
        initExecutor();
        return executorService;
    }

    private static void initExecutor() {
        synchronized (DefaultCacheMonitor.class) {
            if (executorService == null) {
                executorService = Executors.newSingleThreadScheduledExecutor(
                        r -> new Thread(r, "JetCacheMonitorThread"));
            }
        }
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
