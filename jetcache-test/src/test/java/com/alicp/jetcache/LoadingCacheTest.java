package com.alicp.jetcache;

import org.junit.Assert;

import java.util.HashMap;
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

    public static void loadingCacheTest(AbstractCacheBuilder builder) {
        {
            AtomicInteger count = new AtomicInteger(0);
            builder.loader((key) -> key + "_V" + count.getAndIncrement())
                    .batchLoader(null);

            loadingCacheTest(builder.buildCache());
        }
        {
            AtomicInteger count = new AtomicInteger(0);
            builder.loader(null);
            builder.batchLoader((keys) -> {
                Map map = new HashMap();
                ((Set) keys).forEach((k) -> map.put(k, k + "_V" + count.getAndIncrement()));
                return map;
            });
            loadingCacheTest(builder.buildCache());
        }
    }

    private static void loadingCacheTest(Cache cache) {
        Assert.assertEquals("LoadingCache_Key1_V0", cache.get("LoadingCache_Key1"));
        Assert.assertEquals("LoadingCache_Key1_V0", cache.get("LoadingCache_Key1"));

        Set<String> keys = new TreeSet<>();
        keys.add("LoadingCache_Key1");
        keys.add("LoadingCache_Key2");
        keys.add("LoadingCache_Key3");
        Map<Object, Object> map = cache.getAll(keys);
        Assert.assertEquals("LoadingCache_Key1_V0", map.get("LoadingCache_Key1"));
        Assert.assertEquals("LoadingCache_Key2_V1", map.get("LoadingCache_Key2"));
        Assert.assertEquals("LoadingCache_Key3_V2", map.get("LoadingCache_Key3"));
    }
}
