package com.alicp.jetcache.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created on 2016/10/31.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultCacheMonitorManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheMonitorManager.class);

    protected static ScheduledExecutorService executorService;
    protected CopyOnWriteArrayList<DefaultCacheMonitor> monitorList = new CopyOnWriteArrayList();

    private ScheduledFuture<?> future;

    private int resetTime;
    private TimeUnit resetTimeUnit;
    private Consumer<StatInfo> statCallback;
    private boolean verboseLog;

    public DefaultCacheMonitorManager(int resetTime, TimeUnit resetTimeUnit, Consumer<StatInfo> statCallback) {
        this.resetTime = resetTime;
        this.resetTimeUnit = resetTimeUnit;
        this.statCallback = statCallback;
    }

    public DefaultCacheMonitorManager(int resetTime, TimeUnit resetTimeUnit) {
        this(resetTime, resetTimeUnit, false);
    }

    public DefaultCacheMonitorManager(int resetTime, TimeUnit resetTimeUnit, boolean verboseLog) {
        this.resetTime = resetTime;
        this.resetTimeUnit = resetTimeUnit;
        this.verboseLog = verboseLog;
        this.statCallback = this::logStat;
    }

    @PostConstruct
    public synchronized void start() {
        if (future != null) {
            return;
        }
        if (executorService == null) {
            initExecutor();
        }
        Runnable cmd = new Runnable() {
            private long time = System.currentTimeMillis();

            @Override
            public void run() {
                try {
                    List<CacheStat> stats = monitorList.stream().map((m) -> {
                        CacheStat stat = m.getCacheStat();
                        m.resetStat();
                        return stat;
                    }).collect(Collectors.toList());

                    long endTime = System.currentTimeMillis();
                    StatInfo statInfo = new StatInfo();
                    statInfo.setStartTime(time);
                    statInfo.setEndTime(endTime);
                    statInfo.setStats(stats);
                    time = endTime;

                    statCallback.accept(statInfo);
                } catch (Exception e) {
                    logger.error("jetcache DefaultCacheMonitorManager error", e);
                }
            }
        };
        long delay = firstDelay(resetTime, resetTimeUnit);
        future = executorService.scheduleAtFixedRate(cmd, delay, resetTimeUnit.toMillis(resetTime), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public synchronized void stop() {
        future.cancel(false);
        future = null;
    }

    @Deprecated
    public void shutdown() {
        stop();
    }

    public DefaultCacheMonitorManager add(DefaultCacheMonitor... monitors) {
        monitorList.addAll(Arrays.asList(monitors));
        return this;
    }

    public DefaultCacheMonitorManager remove(DefaultCacheMonitor... monitor) {
        monitorList.remove(monitor);
        return this;
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
                        r -> {
                            Thread t = new Thread(r, "JetCacheMonitorThread");
                            t.setDaemon(true);
                            return t;
                        });
            }
        }
    }

    protected static long firstDelay(int resetTime, TimeUnit resetTimeUnit) {
        LocalDateTime firstResetTime = computeFirstResetTime(LocalDateTime.now(), resetTime, resetTimeUnit);
        return firstResetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();
    }

    protected static LocalDateTime computeFirstResetTime(LocalDateTime baseTime, int time, TimeUnit unit) {
        if (unit != TimeUnit.SECONDS && unit != TimeUnit.MINUTES && unit != TimeUnit.HOURS && unit != TimeUnit.DAYS) {
            throw new IllegalArgumentException();
        }
        LocalDateTime t = baseTime;
        switch (unit) {
            case DAYS:
                t = t.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case HOURS:
                if (24 % time == 0) {
                    t = t.plusHours(time - t.getHour() % time);
                } else {
                    t = t.plusHours(1);
                }
                t = t.withMinute(0).withSecond(0).withNano(0);
                break;
            case MINUTES:
                if (60 % time == 0) {
                    t = t.plusMinutes(time - t.getMinute() % time);
                } else {
                    t = t.plusMinutes(1);
                }
                t = t.withSecond(0).withNano(0);
                break;
            case SECONDS:
                if (60 % time == 0) {
                    t = t.plusSeconds(time - t.getSecond() % time);
                } else {
                    t = t.plusSeconds(1);
                }
                t = t.withNano(0);
                break;
        }
        return t;
    }

    private void logStat(StatInfo statInfo) {
        List<CacheStat> stats = statInfo.getStats();
        Collections.sort(stats, (o1, o2) -> {
            if (o1.getCacheName() == null) {
                return -1;
            } else if (o2.getCacheName() == null) {
                return 1;
            } else {
                return o1.getCacheName().compareTo(o2.getCacheName());
            }
        });
        StringBuilder sb;
        if (verboseLog) {
            sb = logVerbose(statInfo);
        } else {
            sb = logStatSummary(statInfo);
        }
        logger.info(sb.toString());
    }

    private StringBuilder logTitle(int initSize, StatInfo statInfo) {
        StringBuilder sb = new StringBuilder(initSize);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        sb.append("jetcache stat from ").append(sdf.format(new Date(statInfo.getStartTime())))
                .append(" to ").append(sdf.format(statInfo.getEndTime())).append("\n");
        return sb;
    }

    private void printSepLine(StringBuilder sb, String title) {
        title.chars().forEach((c) -> {
            if (c == '|') {
                sb.append('+');
            } else {
                sb.append('-');
            }
        });
        sb.append('\n');
    }

    private StringBuilder logStatSummary(StatInfo statInfo) {
        StringBuilder sb = logTitle(2048, statInfo);

        List<CacheStat> stats = statInfo.getStats();
        OptionalInt maxCacheNameLength = stats.stream().mapToInt((s) -> s.getCacheName().length()).max();
        int len = Math.max(5, maxCacheNameLength.orElse(0));

        String title = String.format("%-" + len + "s|%10s|%7s|%14s|%14s|%14s|%14s|%11s|%11s", "cache", "qps", "rate", "get", "hit", "fail", "expire", "avgLoadTime", "maxLoadTime");
        sb.append(title).append('\n');
        printSepLine(sb, title);
        for (CacheStat s : stats) {
            sb.append(String.format("%-" + len + "s", s.getCacheName())).append('|');
            sb.append(String.format("%,10.2f", s.qps())).append('|');
            sb.append(String.format("%6.2f%%", s.hitRate() * 100)).append('|');
            sb.append(String.format("%,14d", s.getGetCount())).append('|');
            sb.append(String.format("%,14d", s.getGetHitCount())).append('|');
            sb.append(String.format("%,14d", s.getGetFailCount())).append('|');
            sb.append(String.format("%,14d", s.getGetExpireCount())).append('|');
            sb.append(String.format("%,11.1f", s.avgLoadTime())).append('|');
            sb.append(String.format("%,11d", s.getMaxLoadTime())).append('\n');
        }
        printSepLine(sb, title);
        return sb;
    }


    private StringBuilder logVerbose(StatInfo statInfo) {
        StringBuilder sb = logTitle(8192, statInfo);
        List<CacheStat> stats = statInfo.getStats();

        for (CacheStat s : stats) {
            String title = String.format("%-10s|%10s|%14s|%14s|%14s|%14s|%14s|%9s|%7s|%7s", "oper", "qps/tps", "count", "success/hit", "fail", "miss", "expired", "avgTime", "minTime", "maxTime");

            printSepLine(sb, title);

            sb.append(s.getCacheName()).append("(hit rate ").append(String.format("%.3f", s.hitRate() * 100)).append("%)").append('\n');
            sb.append(title).append('\n');

            printSepLine(sb, title);

            sb.append(String.format("%-10s", "get")).append('|');
            sb.append(String.format("%,10.2f", s.qps())).append('|');
            sb.append(String.format("%,14d", s.getGetCount())).append('|');
            sb.append(String.format("%,14d", s.getGetHitCount())).append('|');
            sb.append(String.format("%,14d", s.getGetFailCount())).append('|');
            sb.append(String.format("%,14d", s.getGetMissCount())).append('|');
            sb.append(String.format("%,14d", s.getGetExpireCount())).append('|');
            sb.append(String.format("%,9.1f", s.avgGetTime())).append('|');
            sb.append(String.format("%,7d", s.getMinGetTime() == Long.MAX_VALUE ? 0 : s.getMinGetTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxGetTime())).append('\n');

            sb.append(String.format("%-10s", "put")).append('|');
            sb.append(String.format("%,10.2f", s.putTps())).append('|');
            sb.append(String.format("%,14d", s.getPutCount())).append('|');
            sb.append(String.format("%,14d", s.getPutSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getPutFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgPutTime())).append('|');
            sb.append(String.format("%,7d", s.getMinPutTime() == Long.MAX_VALUE ? 0 : s.getMinPutTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxPutTime())).append('\n');

            sb.append(String.format("%-10s", "invalidate")).append('|');
            sb.append(String.format("%,10.2f", s.invalidateTps())).append('|');
            sb.append(String.format("%,14d", s.getInvalidateCount())).append('|');
            sb.append(String.format("%,14d", s.getInvalidateSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getInvalidateFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgInvalidateTime())).append('|');
            sb.append(String.format("%,7d", s.getMinInvalidateTime() == Long.MAX_VALUE ? 0 : s.getMinInvalidateTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxInvalidateTime())).append('\n');

            sb.append(String.format("%-10s", "load")).append('|');
            sb.append(String.format("%,10.2f", s.loadQps())).append('|');
            sb.append(String.format("%,14d", s.getLoadCount())).append('|');
            sb.append(String.format("%,14d", s.getLoadSuccessCount())).append('|');
            sb.append(String.format("%,14d", s.getLoadFailCount())).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%14s", "N/A")).append('|');
            sb.append(String.format("%,9.1f", s.avgLoadTime())).append('|');
            sb.append(String.format("%,7d", s.getMinLoadTime() == Long.MAX_VALUE ? 0 : s.getMinLoadTime())).append('|');
            sb.append(String.format("%,7d", s.getMaxLoadTime())).append('\n');

        }
        return sb;
    }
}
