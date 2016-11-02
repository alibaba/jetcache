package com.alicp.jetcache.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MonitoredCache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/1.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultCacheMonnitorTest {

    public Cache createCache() {
        return EmbeddedCacheBuilder
                .createEmbeddedCacheBuilder()
                .limit(10)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
    }

    @Test
    public void testFirstResetTime() {
        LocalDateTime t = LocalDateTime.of(2016, 11, 11, 23, 50, 33, 123243242);

        LocalDateTime rt = DefaultCacheMonitor.computeFirstResetTime(t, 1, TimeUnit.SECONDS);
        Assert.assertEquals(t.withSecond(34).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 13, TimeUnit.SECONDS);
        Assert.assertEquals(t.withSecond(34).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 30, TimeUnit.SECONDS);
        Assert.assertEquals(t.withMinute(51).withSecond(0).withNano(0), rt);

        rt = DefaultCacheMonitor.computeFirstResetTime(t, 1, TimeUnit.MINUTES);
        Assert.assertEquals(t.withMinute(51).withSecond(0).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 7, TimeUnit.MINUTES);
        Assert.assertEquals(t.withMinute(51).withSecond(0).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 5, TimeUnit.MINUTES);
        Assert.assertEquals(t.withMinute(55).withSecond(0).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 15, TimeUnit.MINUTES);
        Assert.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);

        rt = DefaultCacheMonitor.computeFirstResetTime(t, 1, TimeUnit.HOURS);
        Assert.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 5, TimeUnit.HOURS);
        Assert.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 6, TimeUnit.HOURS);
        Assert.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);

        rt = DefaultCacheMonitor.computeFirstResetTime(t, 1, TimeUnit.DAYS);
        Assert.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);
        rt = DefaultCacheMonitor.computeFirstResetTime(t, 2, TimeUnit.DAYS);
        Assert.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);

        try {
            DefaultCacheMonitor.computeFirstResetTime(t, 1, TimeUnit.MILLISECONDS);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    private void basetest(Cache cache, DefaultCacheMonitor monitor) {
        cache.get("K1");
        cache.put("K1", "V1");
        cache.get("K1");
        cache.invalidate("K1");
        cache.computeIfAbsent("K1", (k) -> null);
        cache.computeIfAbsent("K1", (k) -> null, true);
        cache.get("K1");
        cache.invalidate("K1");

        cache.computeIfAbsent("K2", (k) -> null, false, 10, TimeUnit.SECONDS);
        cache.computeIfAbsent("K2", (k) -> null, true, 10, TimeUnit.SECONDS);
        cache.get("K2");
        cache.invalidate("K2");

        CacheStat s = monitor.getCacheStat();
        Assert.assertEquals(8, s.getGetCount());
        Assert.assertEquals(3, s.getGetHitCount());
        Assert.assertEquals(5, s.getGetMissCount());
        Assert.assertEquals(3, s.getPutCount());
        Assert.assertEquals(3, s.getInvalidateCount());
    }

    @Test
    public void testWithoutLogger() {
        Cache cache = createCache();
        DefaultCacheMonitor monitor = new DefaultCacheMonitor("Test");
        cache = new MonitoredCache(cache, monitor);
        basetest(cache, monitor);
    }

    @Test
    public void testWithLogger() throws Exception {
        DefaultCacheMonitorStatLogger logger = new DefaultCacheMonitorStatLogger();
        Cache c1 = createCache();
        DefaultCacheMonitor m1 = new DefaultCacheMonitor("cache1", 2, TimeUnit.SECONDS, logger);
        Cache c2 = createCache();
        DefaultCacheMonitor m2 = new DefaultCacheMonitor("cache2", 2, TimeUnit.SECONDS, logger);

        c1 = new MonitoredCache(c1, m1);
        c2 = new MonitoredCache(c2, m2);
        basetest(c1, m1);
        basetest(c2, m2);

        Cache mc = new MultiLevelCache(c1, c2);
        DefaultCacheMonitor mcm = new DefaultCacheMonitor("multiCache", 2, TimeUnit.SECONDS, logger);
        mc = new MonitoredCache(mc, mcm);
        basetest(mc, mcm);

//        Thread.sleep(10000);
    }
}
