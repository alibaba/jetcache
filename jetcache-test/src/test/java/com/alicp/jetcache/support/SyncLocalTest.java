/**
 * Created on 2022/5/8.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.MultiLevelCacheBuilder;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.redis.lettuce.JetCacheCodec;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.redis.springdata.RedisSpringDataCacheBuilder;
import com.alicp.jetcache.redisson.RedissonCacheBuilder;
import com.alicp.jetcache.test.anno.TestUtil;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author huangli
 */
public class SyncLocalTest {

    @Test
    public void testJedis() throws Exception {
        String keyPrefix = getClass().getSimpleName() + "testJedis";
        RedisCacheBuilder remoteBuilder1 = RedisCacheBuilder.createRedisCacheBuilder()
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix)
                .jedis(new UnifiedJedis(new HostAndPort("127.0.0.1", 6379)));
        RedisCacheBuilder remoteBuilder2 = RedisCacheBuilder.createRedisCacheBuilder()
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix)
                .jedis(new UnifiedJedis(new HostAndPort("127.0.0.1", 6379)));
        Cache remote1 = remoteBuilder1.buildCache();
        Cache remote2 = remoteBuilder2.buildCache();
        BroadcastManager bm1 = remoteBuilder1.createBroadcastManager(new SimpleCacheManager());
        BroadcastManager bm2 = remoteBuilder2.createBroadcastManager(new SimpleCacheManager());
        test(remote1, remote2, bm1, bm2);
    }

    @Test
    public void testLettuce() throws Exception {
        String keyPrefix = getClass().getSimpleName() + "testLettuce";
        RedisClient client1 = RedisClient.create("redis://127.0.0.1");
        RedisClient client2 = RedisClient.create("redis://127.0.0.1");
        RedisLettuceCacheBuilder remoteBuilder1 = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .redisClient(client1)
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix)
                .pubSubConnection(client1.connectPubSub(new JetCacheCodec()));
        RedisLettuceCacheBuilder remoteBuilder2 = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .redisClient(client2)
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix)
                .pubSubConnection(client2.connectPubSub(new JetCacheCodec()));

        Cache remote1 = remoteBuilder1.buildCache();
        Cache remote2 = remoteBuilder2.buildCache();
        BroadcastManager bm1 = remoteBuilder1.createBroadcastManager(new SimpleCacheManager());
        BroadcastManager bm2 = remoteBuilder2.createBroadcastManager(new SimpleCacheManager());
        test(remote1, remote2, bm1, bm2);
    }

    @Test
    public void testRedisson() throws Exception {
        final String keyPrefix = getClass().getSimpleName() + "testRedisson";
        final Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        RedissonClient client1 = Redisson.create(config);
        RedissonClient client2 = Redisson.create(config);
        RedissonCacheBuilder b1 = RedissonCacheBuilder.createBuilder()
                .redissonClient(client1)
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix);
        RedissonCacheBuilder b2 = RedissonCacheBuilder.createBuilder()
                .redissonClient(client2)
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix);
        test(b1.buildCache(), b2.buildCache(),
                b1.createBroadcastManager(new SimpleCacheManager()),
                b2.createBroadcastManager(new SimpleCacheManager()));
    }

    @Test
    public void testSpringData() throws Exception {
        final String keyPrefix = getClass().getSimpleName() + "testSpringData";
        LettuceConnectionFactory f1 = new LettuceConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", 6379));
        f1.afterPropertiesSet();
        LettuceConnectionFactory f2 = new LettuceConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", 6379));
        f2.afterPropertiesSet();
        RedisSpringDataCacheBuilder b1 = RedisSpringDataCacheBuilder.createBuilder()
                .connectionFactory(f1)
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix);
        RedisSpringDataCacheBuilder b2 = RedisSpringDataCacheBuilder.createBuilder()
                .connectionFactory(f2)
                .keyPrefix(keyPrefix)
                .broadcastChannel(keyPrefix);
        test(b1.buildCache(), b2.buildCache(),
                b1.createBroadcastManager(new SimpleCacheManager()),
                b2.createBroadcastManager(new SimpleCacheManager()));
    }

    private void test(Cache remote1, Cache remote2, BroadcastManager bm1, BroadcastManager bm2) throws Exception {
        CacheManager cacheManager1 = bm1.getCacheManager();
        CacheManager cacheManager2 = bm2.getCacheManager();
        Cache local1 = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .buildCache();
        Cache local2 = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .buildCache();
        bm1.startSubscribe();
        bm2.startSubscribe();

        Cache c1 = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(local1, remote1).buildCache();
        Cache c2 = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(local2, remote2).buildCache();
        cacheManager1.putCache("Area", "CacheName", c1);
        cacheManager2.putCache("Area", "CacheName", c2);
        cacheManager1.putBroadcastManager("Area", bm1);
        cacheManager2.putBroadcastManager("Area", bm2);

        c1.config().getMonitors().add(new CacheNotifyMonitor(cacheManager1, "Area", "CacheName"));
        c2.config().getMonitors().add(new CacheNotifyMonitor(cacheManager2, "Area", "CacheName"));

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

        c1.close();
        c2.close();
        bm1.close();
        bm2.close();
    }
}
