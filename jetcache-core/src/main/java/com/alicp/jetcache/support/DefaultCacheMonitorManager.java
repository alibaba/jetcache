package com.alicp.jetcache.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

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
    private Consumer<List<CacheStat>> resetAction;

    public DefaultCacheMonitorManager(int resetTime, TimeUnit resetTimeUnit, Consumer<List<CacheStat>> resetAction) {
        this.resetAction = resetAction;
        init(resetTime, resetTimeUnit);
    }

    public DefaultCacheMonitorManager(int resetTime, TimeUnit resetTimeUnit) {
        this.resetAction = this::logStat;
        init(resetTime, resetTimeUnit);
    }

    private void init(int resetTime, TimeUnit resetTimeUnit) {
        if (executorService == null) {
            initExecutor();
        }
        Runnable cmd = () -> resetAction.accept(stats());
        long delay = firstDelay(resetTime, resetTimeUnit);
        future = executorService.scheduleAtFixedRate(cmd, delay, resetTimeUnit.toMillis(resetTime), TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        future.cancel(false);
    }

    public DefaultCacheMonitorManager add(DefaultCacheMonitor monitor) {
        monitorList.add(monitor);
        return this;
    }

    public DefaultCacheMonitorManager add(DefaultCacheMonitor... monitors) {
        monitorList.addAll(Arrays.asList(monitors));
        return this;
    }

    public DefaultCacheMonitorManager remove(DefaultCacheMonitor monitor) {
        monitorList.remove(monitor);
        return this;
    }

    public List<CacheStat> stats() {
        DefaultCacheMonitor[] monitorArray = monitorList.toArray(new DefaultCacheMonitor[monitorList.size()]);
        List<CacheStat> stats = new ArrayList<>();
        for (DefaultCacheMonitor m : monitorArray) {
            stats.add(m.getCacheStat());
            m.resetStat();
        }
        return stats;
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

    protected void logStat(List<CacheStat> statsCopy) {
        Collections.sort(statsCopy, (o1, o2) -> {
            if (o1.getCacheName() == null) {
                return -1;
            } else if (o2.getCacheName() == null) {
                return 1;
            } else {
                return o1.getCacheName().compareTo(o2.getCacheName());
            }
        });
        String s = statText(statsCopy).insert(0, "jetcache get stat:\n").toString();
        logger.info(s);
    }

    protected StringBuilder statText(List<CacheStat> statsCopy) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("cache|qps|rate|get|hit|expire|fail|avgLoadTime|maxLoadTime\n");
        sb.append("----------------------------------------------------------\n");
        DecimalFormat hitRateFomater = new DecimalFormat("#%");
        DecimalFormat qpsFomater = new DecimalFormat("#.##");
        for (CacheStat s : statsCopy) {
            sb.append(s.getCacheName()).append('|');
            sb.append(qpsFomater.format(s.qps())).append('|');
            sb.append(hitRateFomater.format(s.hitRate())).append('|');
            sb.append(s.getGetCount()).append('|');
            sb.append(s.getGetHitCount()).append('|');
            sb.append(s.getGetExpireCount()).append('|');
            sb.append(s.getGetFailCount()).append('|');
            sb.append(s.avgLoadTime()).append('|');
            sb.append(s.getMaxLoadTime()).append('\n');
        }
        sb.append("----------------------------------------------------------");
        return sb;
    }
}
