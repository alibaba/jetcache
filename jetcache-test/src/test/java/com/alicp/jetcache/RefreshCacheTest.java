package com.alicp.jetcache;

import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.test.MockRemoteCacheBuilder;
import com.alicp.jetcache.testsupport.Sleeper;
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
        cache.config().setRefreshPolicy(RefreshPolicy.newPolicy(50, TimeUnit.MILLISECONDS));
        Assert.assertEquals("V1", cache.get("K1"));
        Thread.sleep(75);
        Assert.assertEquals("V1", cache.get("K1"));
        ((RefreshCache<Object, Object>)cache).stopRefresh();

        refreshCacheTest(cache, 200, 100);
    }

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

    private Cache buildLocalCache(long expire) {
        return CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(10)
                .expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
    }

    @Test
    public void multiLevelCacheRefreshTest() throws Exception {
        long refresh = 500;
        long expire = 1000000;
        //  use the same remote cache for multilevel caches to be tested,
        //  so we can change the data in remote cache of multilevel cache directly in test purpose.
        Cache remote = new MockRemoteCacheBuilder()
                .limit(10)
                .expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();

        //  build two multilevel cache to test
        MultiLevelCacheBuilder builder1 = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(buildLocalCache(expire), remote);
        MultiLevelCacheBuilder builder2 = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(buildLocalCache(expire), remote);

        //  set refresh policy
        RefreshPolicy policy = RefreshPolicy.newPolicy(refresh, TimeUnit.MILLISECONDS);
        policy.setRefreshLockTimeoutMillis(10000);
        builder1.refreshPolicy(policy);
        builder2.refreshPolicy(policy);

        //  loader for refresh
        AtomicInteger count = new AtomicInteger(0);
        AtomicLong blockMills = new AtomicLong(0);      //  block mills for loader
        CacheLoader loader = (key) -> {
            if (blockMills.get() != 0) {
                Thread.sleep(blockMills.get());
            }
            return key + "_V" + count.getAndIncrement();
        };
        builder1.loader(loader);
        builder2.loader(loader);

        Cache cache1 = builder1.buildCache();
        Cache cache2 = builder2.buildCache();

        multiLevelCacheRefreshTest(cache1, cache2, remote, refresh, blockMills);
        cache1.close();
        cache2.close();
    }

    private void multiLevelCacheRefreshTest(Cache cache1, Cache cache2, Cache remote, long refresh, AtomicLong blockMills) throws InterruptedException {
        //  start test now, set sleep interval by the refresh mills
        Sleeper sleeper = new Sleeper(refresh);

        //  cache1 starts at 0x, cache2 starts at 0.2x (x = refresh mills)
        Assert.assertEquals("K1_V0", cache1.get("K1"));
        sleeper.sleepTo(0.2);
        Assert.assertEquals("K1_V0", cache2.get("K1"));

        //  change blockMills to 0.6x, execution of loader will cause to block 0.6x
        blockMills.set((long) (0.6 * refresh));
        remote.put("K1", "0");

        //  refresh for cache1 starts at 1.0x

        //  now is 1.1x, refresh thread for cache1 is blocked until 1.6x, refresh for cache2 has not been scheduled until 1.2x
        sleeper.sleepTo(1.1);
        Assert.assertEquals("K1_V0", cache1.get("K1"));
        Assert.assertEquals("K1_V0", cache2.get("K1"));
        Assert.assertEquals("0", remote.get("K1"));

        // now is 1.3x, refresh for cache2 starts at 1.5x but failed to get refresh lock, it will refresh from remote on 1.2+0.2=1.4x
        sleeper.sleepTo(1.3);
        Assert.assertEquals("K1_V0", cache1.get("K1"));
        Assert.assertEquals("K1_V0", cache2.get("K1"));
        Assert.assertEquals("0", remote.get("K1"));

        // now is 1.5x
        sleeper.sleepTo(1.5);
        Assert.assertEquals("K1_V0", cache1.get("K1"));
        Assert.assertEquals("0", cache2.get("K1"));
        Assert.assertEquals("0", remote.get("K1"));

        // now is 1.8x, refresh for cache1 finished at 1.6x, cache value has been changed by loader, next refresh will start at 2.6x
        sleeper.sleepTo(1.8);
        Assert.assertEquals("K1_V1", cache1.get("K1"));
        Assert.assertEquals("0", cache2.get("K1"));
        Assert.assertEquals("K1_V1", remote.get("K1"));

        //  the second refresh for cache2 start at 2.2x, but the last refresh timestamp set by cache1 is
        //  less than (now - refreshMills), it's no necessary to execute loader 2 times in one refresh interval,
        //  cache2 refresh upper cache value with remote cache value.
        sleeper.sleepTo(2.4);
        Assert.assertEquals("K1_V1", cache1.get("K1"));
        Assert.assertEquals("K1_V1", cache2.get("K1"));
        Assert.assertEquals("K1_V1", remote.get("K1"));
    }
}
