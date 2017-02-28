package com.alicp.jetcache.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MonitoredCache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/1.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultCacheMonitorTest {

    public Cache createCache() {
        return LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(10)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
    }

    private void basetest(Cache cache, DefaultCacheMonitor monitor) {
        cache.get("K1");
        cache.put("K1", "V1");
        cache.get("K1");
        cache.remove("K1");
        cache.computeIfAbsent("K1", (k) -> null);
        cache.computeIfAbsent("K1", (k) -> null, true);
        cache.get("K1");
        cache.remove("K1");

        cache.computeIfAbsent("K2", (k) -> null, false, 10, TimeUnit.SECONDS);
        cache.computeIfAbsent("K2", (k) -> null, true, 10, TimeUnit.SECONDS);
        cache.get("K2");
        cache.remove("K2");

        CacheStat s = monitor.getCacheStat();
        Assert.assertEquals(8, s.getGetCount());
        Assert.assertEquals(3, s.getGetHitCount());
        Assert.assertEquals(5, s.getGetMissCount());
        Assert.assertEquals(3, s.getPutCount());
        Assert.assertEquals(3, s.getRemoveCount());
        Assert.assertEquals(4, s.getLoadCount());
        Assert.assertEquals(4, s.getLoadSuccessCount());

        monitor.resetStat();
        Map m = new HashMap();
        m.put("multi_k1", "V1");
        m.put("multi_k2", "V2");
        cache.putAll(m);
        HashSet keys = new HashSet(m.keySet());
        keys.add("multi_k3");
        cache.getAll(keys);
        cache.removeAll(keys);
        s = monitor.getCacheStat();
        Assert.assertEquals(3, s.getGetCount());
        Assert.assertEquals(2, s.getGetHitCount());
        Assert.assertEquals(1, s.getGetMissCount());
        Assert.assertEquals(2, s.getPutCount());
        Assert.assertEquals(2, s.getPutSuccessCount());
        Assert.assertEquals(3, s.getRemoveCount());
        Assert.assertEquals(3, s.getRemoveSuccessCount());
        Assert.assertEquals(0, s.getLoadCount());
        Assert.assertEquals(0, s.getLoadSuccessCount());
    }

    @Test
    public void testWithoutLogger() {
        Cache cache = createCache();
        DefaultCacheMonitor monitor = new DefaultCacheMonitor("Test");
        cache = new MonitoredCache(cache, monitor);
        basetest(cache, monitor);
    }

    @Test
    public void testWithManager() throws Exception {
        Cache c1 = createCache();
        DefaultCacheMonitor m1 = new DefaultCacheMonitor("cache1");
        Cache c2 = createCache();
        DefaultCacheMonitor m2 = new DefaultCacheMonitor("cache2");

        c1 = new MonitoredCache(c1, m1);
        c2 = new MonitoredCache(c2, m2);
        DefaultCacheMonitorManager manager = new DefaultCacheMonitorManager(2, TimeUnit.SECONDS);
        manager.start();
        manager.add(m1, m2);

        basetest(c1, m1);
        basetest(c2, m2);

        Cache mc = new MultiLevelCache(c1, c2);
        DefaultCacheMonitor mcm = new DefaultCacheMonitor("multiCache");
        mc = new MonitoredCache(mc, mcm);
        manager.add(mcm);
        basetest(mc, mcm);

        manager.stop();
//        Thread.sleep(10000);
    }
}
