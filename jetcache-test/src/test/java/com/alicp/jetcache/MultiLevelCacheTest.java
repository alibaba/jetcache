package com.alicp.jetcache;

import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import com.alicp.jetcache.support.DefaultCacheMonitorTest;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.test.MockRemoteCache;
import com.alicp.jetcache.test.MockRemoteCacheBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MultiLevelCacheTest extends AbstractCacheTest {

    private Cache<Object, Object> l1Cache;
    private Cache<Object, Object> l2Cache;

    private static final int LIMIT = 1000;

    private void initL1L2(int expireMillis) {
        l1Cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(10)
                .expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
        /*
        l2Cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(LIMIT)
                .expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
        */
        l2Cache = new MockRemoteCacheBuilder()
                .limit(LIMIT)
                .expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
    }

    @Test
    public void testConstructor() {
        try{
            cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder().buildCache();
            Assert.fail();
        } catch (IllegalArgumentException e){
        }
        try{
            new MultiLevelCache(new Cache[0]);
            Assert.fail();
        } catch (IllegalArgumentException e){
        }

        initL1L2(100);
        l1Cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(10)
                .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .loader(key -> null)
                .buildCache();
        try {
            cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache).buildCache();
            Assert.fail();
        } catch (CacheConfigException e) {
        }
    }


    @Test
    public void testUnwrap() {
        initL1L2(100);
        cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache).buildCache();
        Assert.assertTrue(cache.unwrap(LinkedHashMap.class) instanceof LinkedHashMap);
        Assert.assertTrue(cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class) instanceof com.github.benmanes.caffeine.cache.Cache);
    }

    @Test
    public void test() throws Exception {
        initL1L2(200);
        cache = new MultiLevelCache(l1Cache, l2Cache);
        doTest(200);
        expireAfterWriteTest(200);
        DefaultCacheMonitorTest.testMonitor(cache);

        initL1L2(2000);
        cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache).buildCache();
        concurrentTest(200, LIMIT ,3000);

        initL1L2(200);
        LoadingCacheTest.loadingCacheTest(MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache), 0);

        initL1L2(200);
        RefreshCacheTest.refreshCacheTest(MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache), 80, 40);

        doMonitoredTest(200, true, () -> {
            try {
                doTest(200);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        });

        doMonitoredTest(2000, false, () -> {
            try {
                concurrentTest(200, LIMIT , 3000);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        });
    }

    private void doMonitoredTest(int expireMillis, boolean verboseLog, Runnable test) {
        initL1L2(expireMillis);
        DefaultCacheMonitor m1 = new DefaultCacheMonitor("l1");
        DefaultCacheMonitor m1_again = new DefaultCacheMonitor("l1_monitor_again");
        DefaultCacheMonitor m2 = new DefaultCacheMonitor("l2");
        DefaultCacheMonitor mc = new DefaultCacheMonitor("mc");
        l1Cache = new MonitoredCache(l1Cache, m1);
        l1Cache = new MonitoredCache(l1Cache, m1_again);
        l2Cache = new MonitoredCache(l2Cache, m2);
        cache = new MultiLevelCache<>(l1Cache, l2Cache);
        cache = new MonitoredCache<>(cache, mc);
        DefaultCacheMonitorManager logger = new DefaultCacheMonitorManager(1, TimeUnit.SECONDS, verboseLog);
        logger.add(m1, m1_again, m2, mc);
        logger.start();

        test.run();

        logger.stop();
    }

    private void doTest(int expireMillis) throws Exception {
        baseTest();
        expireAfterWriteTest(expireMillis);

        cache.put("KK1", "V1");
        Thread.sleep(15);
        l1Cache.remove("KK1");
        Assert.assertEquals("V1", cache.get("KK1"));

        AbstractCache c1 = getConcreteCache(l1Cache);
        AbstractCache c2 = getConcreteCache(l2Cache);

        CacheValueHolder<Object> h1 = (CacheValueHolder<Object>)
                ((com.github.benmanes.caffeine.cache.Cache) c1.unwrap(com.github.benmanes.caffeine.cache.Cache.class))
                        .getIfPresent("KK1");
        CacheValueHolder<Object> h2 = (CacheValueHolder<Object>)
                ((MockRemoteCache)c2).getHolder("KK1");

        long x = h1.getExpireTime() - h2.getExpireTime();
        if (Math.abs(x) > 10) {
            System.out.println(h1.getExpireTime() + ","  + ((CacheValueHolder) h1.getValue()).getExpireTime());
            System.out.println(h2.getExpireTime() + "," + ((CacheValueHolder) h2.getValue()).getExpireTime());
            Assert.fail();
        }

        testUseExpireOfSubCache();
    }

    private void testUseExpireOfSubCache() throws Exception {
        long oldExpire = l1Cache.config().getExpireAfterWriteInMillis();
        ((MultiLevelCacheConfig<Object, Object>)cache.config()).setUseExpireOfSubCache(true);
        l1Cache.config().setExpireAfterWriteInMillis(15);

        cache.put("useSubExpire_key", "V1");
        Thread.sleep(16);
        Assert.assertNull(l1Cache.get("useSubExpire_key"));
        Assert.assertEquals("V1", cache.get("useSubExpire_key"));
        Assert.assertNotNull(l1Cache.get("useSubExpire_key"));
        Thread.sleep(16);
        Assert.assertNull(l1Cache.get("useSubExpire_key"));
        cache.remove("useSubExpire_key");

        Set s = new HashSet();
        s.add("useSubExpire_key");
        Map m = new HashMap();
        m.put("useSubExpire_key", "V2");
        cache.putAll(m);
        Thread.sleep(16);
        Assert.assertNull(l1Cache.get("useSubExpire_key"));
        Assert.assertEquals("V2", cache.getAll(s).get("useSubExpire_key"));
        Assert.assertNotNull(l1Cache.get("useSubExpire_key"));
        Thread.sleep(16);
        Assert.assertNull(l1Cache.get("useSubExpire_key"));
        cache.remove("useSubExpire_key");

        ((MultiLevelCacheConfig<Object, Object>)cache.config()).setUseExpireOfSubCache(false);
        l1Cache.config().setExpireAfterAccessInMillis(oldExpire);
    }

    private AbstractCache getConcreteCache(Cache c) {
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return (AbstractCache) c;
    }
}
