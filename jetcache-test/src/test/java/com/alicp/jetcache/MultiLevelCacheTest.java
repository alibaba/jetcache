package com.alicp.jetcache;

import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.AbstractCacheTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCacheTest extends AbstractCacheTest {

    private Cache<Object, Object> l1Cache;
    private Cache<Object, Object> l2Cache;

    private void initL1L2(int expireMillis) {
        l1Cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(10)
                .expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
        l2Cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(100000)
                .expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
    }

    @Test
    public void test() throws Exception {
        initL1L2(200);
        cache = new MultiLevelCache(l1Cache, l2Cache);
        doTest(200);

        initL1L2(2000);
        cache = new MultiLevelCache(l1Cache, l2Cache);
        concurrentTest(200, 3000);


        doMonitoredTest(200, () -> {
            try {
                doTest(200);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        });

        doMonitoredTest(2000, () -> {
            try {
                concurrentTest(200, 3000);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        });
    }

    private void doMonitoredTest(int expireMillis, Runnable test) {
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
        DefaultCacheMonitorManager logger = new DefaultCacheMonitorManager(1, TimeUnit.SECONDS, true);
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

        AbstractCache c1 = getAbstractCache(l1Cache);
        AbstractCache c2 = getAbstractCache(l2Cache);

        CacheValueHolder<Object> h1 = (CacheValueHolder<Object>)
                ((com.github.benmanes.caffeine.cache.Cache) c1.unwrap(com.github.benmanes.caffeine.cache.Cache.class))
                        .getIfPresent("KK1");
        CacheValueHolder<Object> h2 = (CacheValueHolder<Object>)
                ((LinkedHashMap) c2.unwrap(LinkedHashMap.class)).get("KK1");

        long x = h1.getExpireTime() - h2.getExpireTime();
        if (Math.abs(x) > 10) {
            System.out.println(h1.getCreateTime() + "," + h1.getExpireTime() + "," + ((CacheValueHolder) h1.getValue()).getCreateTime() + "," + ((CacheValueHolder) h1.getValue()).getExpireTime());
            System.out.println(h2.getCreateTime() + "," + h2.getExpireTime() + "," + ((CacheValueHolder) h2.getValue()).getCreateTime() + "," + ((CacheValueHolder) h2.getValue()).getExpireTime());
            Assert.fail();
        }
    }

    private AbstractCache getAbstractCache(Cache c) {
        while (c instanceof MonitoredCache) {
            c = ((MonitoredCache) c).getTargetCache();
        }
        return (AbstractCache) c;
    }
}
