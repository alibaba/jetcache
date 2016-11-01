package com.alicp.jetcache.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created on 2016/10/31.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultCacheMonitorStatLogger implements Consumer<CacheStat> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheMonitorStatLogger.class);

    private LinkedList<CacheStat> stats = new LinkedList<>();

    @Override
    public synchronized void accept(CacheStat cacheStatCopy) {
        stats.add(cacheStatCopy);
        if (stats.size() == 1) {
            final Object lock = this;
            DefaultCacheMonitor.executorService().schedule(() -> {
                LinkedList<CacheStat> statsCopy;
                synchronized (lock) {
                    statsCopy = new LinkedList(stats);
                    stats.clear();
                }
                logStat(statsCopy);
            }, 5, TimeUnit.SECONDS);
        }
    }

    protected void logStat(LinkedList<CacheStat> statsCopy) {
        Collections.sort(statsCopy, (o1, o2) -> {
            if (o1.getCacheName() == null) {
                return -1;
            } else if (o2.getCacheName() == null) {
                return 1;
            } else {
                return o1.getCacheName().compareTo(o2.getCacheName());
            }
        });
        String s = statText(statsCopy).insert(0, "jetcache stat:\n").toString();
        logger.info(s);
    }

    protected StringBuilder statText(LinkedList<CacheStat> statsCopy) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("cache|get(qps)|hit(rate)|expire|fail|avgLoadTime|maxLoadTime");
        sb.append("----------------------------------------------------------");
        DecimalFormat hitRateFomater = new DecimalFormat("#%");
        DecimalFormat qpsFomater = new DecimalFormat("#.##");
        for (CacheStat s : statsCopy) {
            sb.append(s.getCacheName()).append('|');
            sb.append(s.getGetCount()).append('(').append(qpsFomater.format(s.qps())).append(')').append('|');
            sb.append(s.getGetHitCount()).append('(').append(hitRateFomater.format(s.hitRate())).append(')').append('|');
            sb.append(s.getGetExpireCount()).append('|');
            sb.append(s.getGetFailCount()).append('|');
            sb.append(s.avgLoadTime()).append('|');
            sb.append(s.getMaxGetTime());
        }
        sb.append("----------------------------------------------------------");
        return sb;
    }
}
