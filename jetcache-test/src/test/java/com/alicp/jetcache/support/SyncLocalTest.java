/**
 * Created on 2022/5/8.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MultiLevelCacheBuilder;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SyncLocalTest {
    @Test
    public void test() throws Exception {
        SimpleCacheManager cacheManager1 = new SimpleCacheManager();
        SimpleCacheManager cacheManager2 = new SimpleCacheManager();
        Cache local1 = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .buildCache();
        Cache local2 = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .buildCache();
        RedisCacheBuilder remoteBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                .keyPrefix(SyncLocalTest.class.getName())
                .jedis(new UnifiedJedis(new HostAndPort("127.0.0.1", 6379)));
        CacheMessageConsumer consumer1 = new CacheMessageConsumer("id1", cacheManager1);
        CacheMessageConsumer consumer2 = new CacheMessageConsumer("id2", cacheManager2);
        BroadcastManager broadcastManager = remoteBuilder.createBroadcastManager();
        broadcastManager.startSubscribe(m -> {
            consumer1.accept(m);
            consumer2.accept(m);
        });
        Cache remote1 = remoteBuilder.buildCache();
        Cache remote2 = remoteBuilder.buildCache();
        Cache c1 = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(local1, remote1).buildCache();
        Cache c2 = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(local2, remote2).buildCache();
        cacheManager1.putCache("Area", "CacheName", c1);
        cacheManager2.putCache("Area", "CacheName", c2);

        c1.config().getMonitors().add(new CacheNotifyMonitor(broadcastManager, "Area", "CacheName", c1, "id1"));
        c2.config().getMonitors().add(new CacheNotifyMonitor(broadcastManager, "Area", "CacheName", c2, "id2"));

        Thread.sleep(50);

        local1.put("K1", "V1");
        local2.put("K1", "V1");
        Assertions.assertEquals("V1", local1.get("K1"));
        Assertions.assertEquals("V1", local2.get("K1"));
        c1.put("K1", "V2");
        TestUtil.waitUtil(null, () -> local2.get("K1"));
        Assertions.assertEquals("V2", local1.get("K1"));


        local1.put("K1", "V1");
        local2.put("K1", "V1");
        Assertions.assertEquals("V1", local1.get("K1"));
        Assertions.assertEquals("V1", local2.get("K1"));
        c2.remove("K1");
        TestUtil.waitUtil(null, () -> local1.get("K1"));
        Assertions.assertEquals(null, local2.get("K1"));

        local1.put("K1", "V1");
        local2.put("K1", "V1");
        local1.put("K2", "V2");
        local2.put("K2", "V2");
        HashMap<String, String> m = new HashMap<>();
        m.put("K2", "V2_new");
        c1.putAll(m);
        TestUtil.waitUtil(null, () -> local2.get("K2"));
        Assertions.assertEquals("V1", local2.get("K1"));
        Assertions.assertEquals("V1", local1.get("K1"));
        Assertions.assertEquals("V2_new", local1.get("K2"));

        HashSet<String> s = new HashSet<>();
        s.add("K1");
        s.add("K2");
        c2.removeAll(s);
        TestUtil.waitUtil(null, () -> local1.get("K1"));
        TestUtil.waitUtil(null, () -> local1.get("K2"));
    }
}
