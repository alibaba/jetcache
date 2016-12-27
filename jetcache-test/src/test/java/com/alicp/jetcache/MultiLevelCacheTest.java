package com.alicp.jetcache;

import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.AbstractCacheTest;
import org.junit.Assert;
import org.junit.Test;

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

        cache.put("K1", "V1");
        Thread.sleep(15);
        l1Cache.remove("K1");
        Assert.assertEquals("V1", cache.get("K1"));
        CacheGetResult<CacheValueHolder<Object>> h1 = ((AbstractCache) l1Cache).getHolder("K1");
        CacheGetResult<CacheValueHolder<Object>> h2 = ((AbstractCache) l2Cache).getHolder("K1");
        Assert.assertEquals(h1.getValue().getExpireTime(), h2.getValue().getExpireTime());
    }
}
