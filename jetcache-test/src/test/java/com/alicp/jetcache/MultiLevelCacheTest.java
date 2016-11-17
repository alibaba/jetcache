package com.alicp.jetcache;

import com.alicp.jetcache.embedded.CaffeineCache;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiLevelCacheTest extends AbstractCacheTest {

    private WrapValueCache<Object, Object> l1Cache;
    private WrapValueCache<Object, Object> l2Cache;

    private void initL1L2(){
        l1Cache = (AbstractCache<Object, Object>) EmbeddedCacheBuilder
                .createEmbeddedCacheBuilder()
                .limit(10)
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new CaffeineCache((EmbeddedCacheConfig) c))
                .buildCache();
        l2Cache = (AbstractCache<Object, Object>) EmbeddedCacheBuilder
                .createEmbeddedCacheBuilder()
                .limit(100000)
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .softValues()
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .buildCache();
    }

    @Test
    public void test() throws Exception {

        initL1L2();
        cache = new MultiLevelCache(l1Cache, l2Cache);
        doTest();


        initL1L2();
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
        doTest();
        logger.shutdown();
    }

    private void doTest() throws Exception {
        baseTest();
        expireAfterWriteTest(200);
        concurrentTest(20, 1000, 3000);

        cache.put("K1", "V1");
        Thread.sleep(10);
        l1Cache.invalidate("K1");
        Assert.assertEquals("V1", cache.get("K1"));
        CacheGetResult<CacheValueHolder<Object>> h1 = l1Cache.GET_HOLDER("K1");
        CacheGetResult<CacheValueHolder<Object>> h2 = l2Cache.GET_HOLDER("K1");
        Assert.assertEquals(h1.getValue().getExpireTime(), h2.getValue().getExpireTime());
    }
}
