package com.alicp.jetcache;

import com.alicp.jetcache.support.DefaultCacheMonitor;
import org.junit.Assert;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017/5/24.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LoadingCacheTest {

    public static void loadingCacheTest1(AbstractCacheBuilder builder) {
        AtomicInteger count = new AtomicInteger(0);
        builder.loader((key) -> key + "_V" + count.getAndIncrement());
        loadingCacheTest(builder.buildCache());
    }


    private static void loadingCacheTest(Cache cache) {
        DefaultCacheMonitor monitor = new DefaultCacheMonitor("test");
        cache.config().getMonitors().add(monitor);

        Assert.assertEquals("LoadingCache_Key1_V0", cache.get("LoadingCache_Key1"));
        Assert.assertEquals(1, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(0, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(1, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(1, monitor.getCacheStat().getLoadCount());
        Assert.assertEquals("LoadingCache_Key1_V0", cache.get("LoadingCache_Key1"));
        Assert.assertEquals(2, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(1, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(1, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(1, monitor.getCacheStat().getLoadCount());

        Set<String> keys = new TreeSet<>();
        keys.add("LoadingCache_Key1");
        keys.add("LoadingCache_Key2");
        keys.add("LoadingCache_Key3");
        Map<Object, Object> map = cache.getAll(keys);
        Assert.assertEquals("LoadingCache_Key1_V0", map.get("LoadingCache_Key1"));
        Assert.assertEquals("LoadingCache_Key2_V1", map.get("LoadingCache_Key2"));
        Assert.assertEquals("LoadingCache_Key3_V2", map.get("LoadingCache_Key3"));

        Assert.assertEquals(5, monitor.getCacheStat().getGetCount());
        Assert.assertEquals(2, monitor.getCacheStat().getGetHitCount());
        Assert.assertEquals(3, monitor.getCacheStat().getGetMissCount());
        Assert.assertEquals(3, monitor.getCacheStat().getLoadCount());
    }
}
