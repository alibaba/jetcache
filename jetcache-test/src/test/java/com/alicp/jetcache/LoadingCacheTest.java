package com.alicp.jetcache;

import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.test.AbstractCacheTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017/5/24.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LoadingCacheTest extends AbstractCacheTest {

    @Test
    public void test() throws Exception {
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .buildCache();
        cache = new LoadingCache<>(cache);
        baseTest();
        AtomicInteger count = new AtomicInteger(0);
        cache.config().setLoader((key) -> key + "_V" + count.getAndIncrement());
        loadingCacheTest(cache);
    }

    public static void loadingCacheTest(AbstractCacheBuilder builder) {
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

        cache.config().getMonitors().remove(monitor);
    }
}
