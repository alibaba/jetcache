package com.alicp.jetcache;

import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.test.AbstractCacheTest;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2017/5/31.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RefreshCacheTest extends AbstractCacheTest {
    public static void refreshCacheTest(Cache cache, long refresh, long stopRefreshAfterLastAccess) throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        CacheLoader oldLoader = cache.config().getLoader();
        RefreshPolicy oldPolicy = cache.config().getRefreshPolicy();

        cache.config().setLoader((key) -> key + "_V" + count.getAndIncrement());
        RefreshPolicy policy = RefreshPolicy.newPolicy(refresh, TimeUnit.MILLISECONDS)
                .refreshLockTimeout(10, TimeUnit.SECONDS);
        cache.config().setRefreshPolicy(policy);
        refreshCacheTest1(cache);
        getRefreshCache(cache).stopRefresh();

        count.set(0);
        cache.config().getRefreshPolicy().setStopRefreshAfterLastAccessMillis(stopRefreshAfterLastAccess);
        refreshCacheTest2(cache);
        getRefreshCache(cache).stopRefresh();

        cache.config().setLoader(oldLoader);
        cache.config().setRefreshPolicy(oldPolicy);
    }

    public static void refreshCacheTest(AbstractCacheBuilder builder, long refresh, long stopRefreshAfterLastAccess) throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        builder.loader((key) -> key + "_V" + count.getAndIncrement());
        RefreshPolicy policy = RefreshPolicy.newPolicy(refresh, TimeUnit.MILLISECONDS);
        policy.setRefreshLockTimeoutMillis(10000);
        builder.refreshPolicy(policy);
        Cache cache = builder.buildCache();
        refreshCacheTest1(cache);
        cache.close();

        count.set(0);
        builder.getConfig().getRefreshPolicy().stopRefreshAfterLastAccess(stopRefreshAfterLastAccess, TimeUnit.MILLISECONDS);
        cache = builder.buildCache();
        refreshCacheTest2(cache);
        cache.close();
    }

    public static void refreshUpperCacheTest(MultiLevelCacheBuilder builder,
                                             MultiLevelCacheBuilder builder2,
                                             Cache<Object, Object> remote, long refresh) throws Exception {
        RefreshPolicy policy = RefreshPolicy.newPolicy(refresh, TimeUnit.MILLISECONDS);
        policy.setRefreshLockTimeoutMillis(10000);
        builder.refreshPolicy(policy);
        builder2.refreshPolicy(policy);

        AtomicInteger count = new AtomicInteger(0);
        AtomicLong blockMills = new AtomicLong(0);
        CacheLoader loader = (key) -> {
            if (blockMills.get() != 0) Thread.sleep(blockMills.get());
            return key + "_V" + count.getAndIncrement();
        };
        builder.loader(loader);
        builder2.loader(loader);

        Cache cache = builder.buildCache();
        Cache cache2 = builder2.buildCache();

        testLockFailAndRefreshUpperCache(cache, cache2, remote, refresh, blockMills);
        cache.close();
        cache2.close();
        remote.close();
    }

    private static void testLockFailAndRefreshUpperCache(Cache cache, Cache cache2, Cache remote, long refresh, AtomicLong blockMills) throws InterruptedException {
        RefreshSleeper sleeper = new RefreshSleeper(refresh);                     //  以refresh间隔为基准x

        Assert.assertEquals("K1_V0", cache.get("K1"));
        sleeper.sleepTo(0.5);
        Assert.assertEquals("K1_V0", cache2.get("K1"));     //  在0.5x时间点启动cache2

        blockMills.set((long) (0.8*refresh));                        //  loader函数内将阻塞0.8x
        remote.put("K1", "0");                                       //  手工将remote缓存置为0

        sleeper.sleepTo(1.2);
        Assert.assertEquals("K1_V0", cache.get("K1"));
        Assert.assertEquals("K1_V0", cache2.get("K1"));

        sleeper.sleepTo(1.7);
        Assert.assertEquals("K1_V0", cache.get("K1"));  //  当前时间为1.7x，cache的第一次load将于1.8x执行完毕，此时loader仍然被阻塞
        Assert.assertEquals("K1_V0", cache2.get("K1"));     //  cache2在1.5x执行load，此时lock被cache占用，将放弃执行load

        sleeper.sleepTo(1.9);
        Assert.assertEquals("K1_V1", cache.get("K1"));  //  cache的第1次load完成
        Assert.assertEquals("K1_V0", cache2.get("K1"));

        //  cache2的第2次load在2.5x开始，由于上次load完成时间是1.8x，因此也不执行load，从remote获取值，更新local
        sleeper.sleepTo(2.7);
        Assert.assertEquals("K1_V1", cache.get("K1"));
        Assert.assertEquals("K1_V1", cache2.get("K1"));
    }

    /**
     * sleep帮助类，以refresh为间隔基准，start为开始时间，可以通过sleepTo方法sleep至基准的任意倍数。
     */
    private static class RefreshSleeper {
        private long refresh;
        private long start;

        public RefreshSleeper(long refresh) {
            this.refresh = refresh;
            start = System.currentTimeMillis();
        }

        public void sleepTo(double ratio) throws InterruptedException {
            long wakeup = (long) (start + ratio * refresh);
            while (System.currentTimeMillis() < wakeup)
                Thread.sleep((long) (refresh * 0.02));
        }
    }

    private static RefreshCache getRefreshCache(Cache cache) {
        Cache c = cache;
        while (!(c instanceof RefreshCache)) {
            if (c instanceof ProxyCache) {
                c = ((ProxyCache) c).getTargetCache();
            }
        }
        return (RefreshCache) c;
    }

    private static boolean isMultiLevelCache(Cache cache) {
        Cache c = cache;
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return c instanceof MultiLevelCache;
    }

    private static void refreshCacheTest1(Cache cache) throws Exception {
        DefaultCacheMonitor monitor = new DefaultCacheMonitor("test");
        cache.config().getMonitors().add(monitor);
        long refreshMillis = cache.config().getRefreshPolicy().getRefreshMillis();

        Assert.assertEquals("refreshCacheTest1_K1_V0", cache.get("refreshCacheTest1_K1"));
        Assert.assertEquals(1, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(0, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(1, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(1, monitor.getCacheStat().getLoadCount());
        Assert.assertEquals(1, monitor.getCacheStat().getPutCount());
        Assert.assertEquals("refreshCacheTest1_K2_V1", cache.get("refreshCacheTest1_K2"));
        Assert.assertEquals(2, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(0, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(2, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(2, monitor.getCacheStat().getLoadCount());
        Assert.assertEquals(2, monitor.getCacheStat().getPutCount());
        Assert.assertEquals("refreshCacheTest1_K1_V0", cache.get("refreshCacheTest1_K1"));
        Assert.assertEquals(3, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(1, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(2, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(2, monitor.getCacheStat().getLoadCount());
        Assert.assertEquals(2, monitor.getCacheStat().getPutCount());
        Assert.assertEquals("refreshCacheTest1_K2_V1", cache.get("refreshCacheTest1_K2"));
        Assert.assertEquals(4, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(2, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(2, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(2, monitor.getCacheStat().getLoadCount());
        Assert.assertEquals(2, monitor.getCacheStat().getPutCount());

        Thread.sleep((long) (1.5 * refreshMillis));

        boolean external = getRefreshCache(cache).concreteCache() instanceof AbstractExternalCache;
        boolean multiLevel = isMultiLevelCache(cache);

        Assert.assertEquals(4, monitor.getCacheStat().getLoadCount());
        Assert.assertNotEquals("refreshCacheTest1_K1_V0", cache.get("refreshCacheTest1_K1"));
        if (external && !multiLevel) {
            Assert.assertEquals(5 + 2/*timestamp*/, monitor.getCacheStat().getGetCount());
            Assert.assertEquals(3, monitor.getCacheStat().getGetHitCount());
            Assert.assertEquals(2 + 2/*timestamp*/, monitor.getCacheStat().getGetMissCount());
            Assert.assertEquals(4, monitor.getCacheStat().getLoadCount());
            Assert.assertEquals(4 + 2/*timestamp*/ + 2/*tryLock -> putIfAbsent*/, monitor.getCacheStat().getPutCount());
        } else {
            Assert.assertEquals(5, monitor.getCacheStat().getGetCount());
            Assert.assertEquals(3, monitor.getCacheStat().getGetHitCount());
            Assert.assertEquals(2, monitor.getCacheStat().getGetMissCount());
            Assert.assertEquals(4, monitor.getCacheStat().getLoadCount());
            Assert.assertEquals(4, monitor.getCacheStat().getPutCount());
        }
        Assert.assertNotEquals("refreshCacheTest1_K2_V1", cache.get("refreshCacheTest1_K2"));
        if (external && !multiLevel) {
            Assert.assertEquals(6 + 2, monitor.getCacheStat().getGetCount());
            Assert.assertEquals(4, monitor.getCacheStat().getGetHitCount());
            Assert.assertEquals(2 + 2, monitor.getCacheStat().getGetMissCount());
            Assert.assertEquals(4, monitor.getCacheStat().getLoadCount());
            Assert.assertEquals(4 + 2 + 2, monitor.getCacheStat().getPutCount());
        } else {
            Assert.assertEquals(6, monitor.getCacheStat().getGetCount());
            Assert.assertEquals(4, monitor.getCacheStat().getGetHitCount());
            Assert.assertEquals(2, monitor.getCacheStat().getGetMissCount());
            Assert.assertEquals(4, monitor.getCacheStat().getLoadCount());
            Assert.assertEquals(4, monitor.getCacheStat().getPutCount());
        }

        cache.config().getMonitors().remove(monitor);
    }

    private static void refreshCacheTest2(Cache cache) throws Exception {
        DefaultCacheMonitor monitor = new DefaultCacheMonitor("test");
        cache.config().getMonitors().add(monitor);
        long refreshMillis = cache.config().getRefreshPolicy().getRefreshMillis();
        long stopRefresh = cache.config().getRefreshPolicy().getStopRefreshAfterLastAccessMillis();

        Set s = new HashSet();
        s.add("refreshCacheTest2_K1");
        s.add("refreshCacheTest2_K2");
        Map values = cache.getAll(s);
        long key1StartRefreshTime = System.currentTimeMillis();

        Assert.assertEquals(2, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(0, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(2, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(2, monitor.getCacheStat().getLoadCount());
        Assert.assertEquals(2, monitor.getCacheStat().getPutCount());

        while (true) {
            long sleepTime = stopRefresh / 5;
            Thread.sleep(sleepTime);
            cache.get("refreshCacheTest2_K1");
            long totalSpendTime = System.currentTimeMillis() - key1StartRefreshTime;
            if (totalSpendTime > 1.4 * refreshMillis) {
                break;
            }
        }

        cache.config().setLoader(null);//stop refresh

        Assert.assertEquals(3, monitor.getCacheStat().getLoadCount());
        Object newK1Value = cache.get("refreshCacheTest2_K1");
        Assert.assertNotEquals(values.get("refreshCacheTest2_K1"), newK1Value);
        Assert.assertEquals(3, monitor.getCacheStat().getLoadCount());
        // refresh task stopped, but K/V is not expires
        Assert.assertEquals(values.get("refreshCacheTest2_K2"), cache.get("refreshCacheTest2_K2"));
        Assert.assertEquals(3, monitor.getCacheStat().getLoadCount());

        Thread.sleep(refreshMillis);
        Assert.assertEquals(newK1Value, cache.get("refreshCacheTest2_K1"));

        cache.config().getMonitors().remove(monitor);
    }

    @Test
    public void test() throws Exception {
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .buildCache();
        cache = new MonitoredCache<>(cache);
        cache = new RefreshCache<>(cache);
        baseTest();

        cache.put("K1", "V1");
        cache.config().setLoader(k -> {
            throw new SQLException();
        });
        cache.config().setRefreshPolicy(RefreshPolicy.newPolicy(30, TimeUnit.MILLISECONDS));
        Assert.assertEquals("V1", cache.get("K1"));
        Thread.sleep(45);
        Assert.assertEquals("V1", cache.get("K1"));
        ((RefreshCache<Object, Object>) cache).stopRefresh();

        refreshCacheTest(cache, 80, 40);
    }
}
