package com.alicp.jetcache.redis;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.LoadingCacheTest;
import com.alicp.jetcache.RefreshCacheTest;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheTest;
import com.alicp.jetcache.support.*;
import com.alicp.jetcache.test.external.AbstractExternalCacheTest;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.util.Pool;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisCacheTest extends AbstractExternalCacheTest {

    @Test
    public void testSimplePool() throws Exception {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPool pool = new JedisPool(pc, "127.0.0.1", 6379);

        testImpl(pool);
    }

    @Test
    public void testJedisPooled() throws Exception {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPooled jedis = new JedisPooled(pc, "127.0.0.1", 6379);

        testImpl(jedis);
    }

    @Test
    public void testSentinel() throws Exception {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);

        Set<String> sentinels = new HashSet<>();
        sentinels.add("127.0.0.1:26379");
        sentinels.add("127.0.0.1:26380");
        sentinels.add("127.0.0.1:26381");
        JedisSentinelPool pool = new JedisSentinelPool("mymaster", sentinels, pc);

        testImpl(pool);
    }

    @Test
    public void testCluster() throws Exception {
        if (!RedisLettuceCacheTest.checkOS()) {
            return;
        }
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7000));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7001));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7002));
        JedisCluster jedis = new JedisCluster(jedisClusterNodes);
        testImpl(jedis);
    }

    private RedisCacheBuilder createCacheBuilder(Object jedis) {
        RedisCacheBuilder builder = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "");
        if (jedis instanceof Pool) {
            builder.jedisPool((Pool<Jedis>) jedis);
        } else {
            builder.jedis((UnifiedJedis) jedis);
        }
        return builder;
    }

    private void testImpl(Object jedis) throws Exception {
        cache = createCacheBuilder(jedis)
                .expireAfterWrite(500, TimeUnit.MILLISECONDS)
                .buildCache();

        if (jedis instanceof JedisPooled) {
            Assert.assertSame(jedis, cache.unwrap(JedisPooled.class));
        } else if (jedis instanceof JedisCluster) {
            Assert.assertSame(jedis, cache.unwrap(JedisCluster.class));
        } else if (jedis instanceof Pool) {
            Assert.assertSame(jedis, cache.unwrap(Pool.class));
        }

        baseTest();
        fastjsonKeyCoverterTest();
        expireAfterWriteTest(cache.config().getExpireAfterWriteInMillis());

        LoadingCacheTest.loadingCacheTest(createCacheBuilder(jedis), 0);
        RefreshCacheTest.refreshCacheTest(createCacheBuilder(jedis), 200, 100);


        cache = createCacheBuilder(jedis).buildCache();
        nullKeyConvertorTest();

        int thread = 10;
        int time = 3000;
        cache = createCacheBuilder(jedis).buildCache();
        concurrentTest(thread, 500, time);
    }

    @Test
    public void testRandomIndex() {
        {
            int[] ws = new int[]{100, 100, 100};
            int[] result = new int[3];
            for (int i = 0; i < 10000; i++) {
                int index = RedisCache.randomIndex(ws);
                result[index]++;
            }
            Assert.assertEquals(1.0, 1.0 * result[1] / result[0], 0.2);
            Assert.assertEquals(1.0, 1.0 * result[2] / result[0], 0.2);
        }
        {
            int[] ws = new int[]{1, 2, 3};
            int[] result = new int[3];
            for (int i = 0; i < 10000; i++) {
                int index = RedisCache.randomIndex(ws);
                result[index]++;
            }
            Assert.assertEquals(2.0, 1.0 * result[1] / result[0], 0.2);
            Assert.assertEquals(3.0, 1.0 * result[2] / result[0], 0.4);
        }
    }

    @Test
    public void readFromSlaveTest() throws Exception {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPool pool1 = new JedisPool(pc, "127.0.0.1", 6379);
        JedisPool pool2 = new JedisPool(pc, "127.0.0.1", 6380);
        JedisPool pool3 = new JedisPool(pc, "127.0.0.1", 6381);

        RedisCacheBuilder builder = RedisCacheBuilder.createRedisCacheBuilder();
        builder.setJedisPool(pool1);
        builder.setReadFromSlave(true);
        builder.setJedisSlavePools(pool2, pool3);
        builder.setSlaveReadWeights(1, 1);
        builder.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        builder.setValueEncoder(JavaValueEncoder.INSTANCE);
        builder.setValueDecoder(JavaValueDecoder.INSTANCE);
        builder.setKeyPrefix(new Random().nextInt() + "");
        builder.setExpireAfterWriteInMillis(500);

        readFromSlaveTestAsserts(pool1, builder);

        builder.setSlaveReadWeights(null);
        readFromSlaveTestAsserts(pool1, builder);
    }

    private void readFromSlaveTestAsserts(JedisPool pool1, RedisCacheBuilder builder) throws InterruptedException {
        Cache cache = builder.buildCache();
        cache.put("readFromSlaveTest_K1", "V1");
        Assert.assertNotSame(pool1, ((RedisCache) cache).readCommands());
        Assert.assertNotSame(pool1, ((RedisCache) cache).readCommands());
        Assert.assertNotSame(pool1, ((RedisCache) cache).readCommands());
        Assert.assertNotSame(pool1, ((RedisCache) cache).readCommands());
        Thread.sleep(15);
        Assert.assertEquals("V1", cache.get("readFromSlaveTest_K1"));
        Assert.assertEquals("V1", cache.get("readFromSlaveTest_K1"));
        Assert.assertEquals("V1", cache.get("readFromSlaveTest_K1"));
        Assert.assertEquals("V1", cache.get("readFromSlaveTest_K1"));
    }
}
