package com.alicp.jetcache.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alicp.jetcache.test.anno.TestUtil.waitUtil;

/**
 * Created on 2016/11/1.
 *
 * @author huangli
 */
public class DefaultCacheMonitorTest {

    public Cache createCache() {
        return LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(10)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
    }

    private static void basetest(Cache cache, DefaultCacheMonitor m) {
        {
            CacheStat oldStat = m.getCacheStat();
            cache.get("MONITOR_TEST_K1");
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetMissCount() + 1 == s.getGetMissCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.put("MONITOR_TEST_K1", "V1");
            waitUtil(oldStat.getPutCount() + 1, () -> m.getCacheStat().getPutCount());
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.get("MONITOR_TEST_K1");
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetHitCount() + 1 == s.getGetHitCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.remove("MONITOR_TEST_K1");
            waitUtil(oldStat.getRemoveCount() + 1, () -> m.getCacheStat().getRemoveCount());
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.computeIfAbsent("MONITOR_TEST_K1", (k) -> null);
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetMissCount() + 1 == s.getGetMissCount()
                        && oldStat.getPutCount() == s.getPutCount()
                        && oldStat.getLoadCount() + 1 == s.getLoadCount()
                        && oldStat.getLoadSuccessCount() + 1 == s.getLoadSuccessCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.computeIfAbsent("MONITOR_TEST_K1", (k) -> null, true);
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetMissCount() + 1 == s.getGetMissCount()
                        && oldStat.getPutCount() + 1 == s.getPutCount()
                        && oldStat.getLoadCount() + 1 == s.getLoadCount()
                        && oldStat.getLoadSuccessCount() + 1 == s.getLoadSuccessCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.get("MONITOR_TEST_K1");
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetHitCount() + 1 == s.getGetHitCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.remove("MONITOR_TEST_K1");
            waitUtil(oldStat.getRemoveCount() + 1, () -> m.getCacheStat().getRemoveCount());
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.computeIfAbsent("MONITOR_TEST_K2", (k) -> null, false, 10, TimeUnit.SECONDS);
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetMissCount() + 1 == s.getGetMissCount()
                        && oldStat.getPutCount() == s.getPutCount()
                        && oldStat.getLoadCount() + 1 == s.getLoadCount()
                        && oldStat.getLoadSuccessCount() + 1 == s.getLoadSuccessCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.computeIfAbsent("MONITOR_TEST_K2", (k) -> null, true, 10, TimeUnit.SECONDS);
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetMissCount() + 1 == s.getGetMissCount()
                        && oldStat.getPutCount() + 1 == s.getPutCount()
                        && oldStat.getLoadCount() + 1 == s.getLoadCount()
                        && oldStat.getLoadSuccessCount() + 1 == s.getLoadSuccessCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.get("MONITOR_TEST_K2");
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 1 == s.getGetCount()
                        && oldStat.getGetHitCount() + 1 == s.getGetHitCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            cache.remove("MONITOR_TEST_K2");
            waitUtil(oldStat.getRemoveCount() + 1, () -> m.getCacheStat().getRemoveCount());
        }

        Map map = new HashMap();
        map.put("MONITOR_TEST_multi_k1", "V1");
        map.put("MONITOR_TEST_multi_k2", "V2");
        {
            CacheStat oldStat = m.getCacheStat();
            cache.putAll(map);
            waitUtil(oldStat.getPutCount() + 2, () -> m.getCacheStat().getPutCount());
        }

        {
            CacheStat oldStat = m.getCacheStat();
            HashSet keys = new HashSet(map.keySet());
            keys.add("MONITOR_TEST_multi_k3");
            cache.getAll(keys);
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getGetCount() + 3 == s.getGetCount()
                        && oldStat.getGetHitCount() + 2 == s.getGetHitCount()
                        && oldStat.getGetMissCount() + 1 == s.getGetMissCount();
            });
        }

        {
            CacheStat oldStat = m.getCacheStat();
            HashSet keys = new HashSet(map.keySet());
            cache.removeAll(keys);
            waitUtil(() -> {
                CacheStat s = m.getCacheStat();
                return oldStat.getRemoveCount() + 2 == s.getRemoveCount()
                        && oldStat.getRemoveSuccessCount() + 2 == s.getRemoveSuccessCount();
            });
        }
    }

    public static void testMonitor(Cache cache) {
        // new style test
        List monitors = cache.config().getMonitors();
        monitors.clear();
        DefaultCacheMonitor monitor = new DefaultCacheMonitor("Test");
        monitors.add(monitor);
        basetest(cache, monitor);
    }

    @Test
    public void testWithoutLogger() {
        Cache cache = createCache();
        testMonitor(cache);
    }

    @Test
    public void testWithManager() throws Exception {
        Cache c1 = createCache();
        DefaultCacheMonitor m1 = new DefaultCacheMonitor("cache1");
        Cache c2 = createCache();
        DefaultCacheMonitor m2 = new DefaultCacheMonitor("cache2");

        c1.config().getMonitors().add(m1);
        c2.config().getMonitors().add(m2);
        DefaultMetricsManager manager = new DefaultMetricsManager(10, TimeUnit.SECONDS, true);
        manager.start();
        manager.add(m1, m2);

        basetest(c1, m1);
        basetest(c2, m2);

        Cache mc = new MultiLevelCache(c1, c2);
        DefaultCacheMonitor mcm = new DefaultCacheMonitor("multiCache");
        mc.config().getMonitors().add(mcm);
        manager.add(mcm);
        basetest(mc, mcm);


        manager.cmd.run();
        manager.stop();
//        Thread.sleep(10000);
    }
}
