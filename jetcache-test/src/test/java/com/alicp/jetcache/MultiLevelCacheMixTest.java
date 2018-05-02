/**
 * Created on 2018/1/30.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.alicp.jetcache.test.MockRemoteCacheBuilder;
import io.lettuce.core.RedisClient;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MultiLevelCacheMixTest {

    private Cache<Object, Object> cache;
    private Cache<Object, Object> l1Cache;
    private Cache<Object, Object> l2Cache;

    @Test
    public void testWithMockRemoteCache() {
        l1Cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(10)
                .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
        l2Cache = new MockRemoteCacheBuilder()
                .limit(1000)
                .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
        cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache).buildCache();
        testImpl();
    }

    @Test
    public void testWithJedis() {
        l1Cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(10)
                .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(1);
        pc.setMaxIdle(1);
        pc.setMaxTotal(2);
        JedisPool pool = new JedisPool(pc, "localhost", 6379);
        l2Cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
                .buildCache();
        cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache).buildCache();
        testImpl();
    }

    @Test
    public void testWithLettuce() {
        l1Cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(10)
                .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        l2Cache = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .redisClient(client)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(1000, TimeUnit.MILLISECONDS)
                .buildCache();
        cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder().addCache(l1Cache, l2Cache).buildCache();
        testImpl();
    }

    private void testImpl() {
        simpleTest();
        testTopCachePut();
        testSubCachePut();
        testCompatibilityBefore_2_5_0();
    }

    private void simpleTest() {
        cache.put("SIMPLE_K1", "V1");
        assertEquals("V1", l1Cache.get("SIMPLE_K1"));
        assertEquals("V1", l2Cache.get("SIMPLE_K1"));
        cache.remove("SIMPLE_K1");

        l1Cache.put("SIMPLE_K2", "V2");
        assertEquals("V2", cache.get("SIMPLE_K2"));
        assertNull(l2Cache.get("SIMPLE_K2"));
        cache.remove("SIMPLE_K2");

        l2Cache.put("SIMPLE_K3", "V3");
        assertNull(l1Cache.get("SIMPLE_K3"));
        assertEquals("V3", cache.get("SIMPLE_K3"));
        assertEquals("V3", l1Cache.get("SIMPLE_K3"));
        cache.remove("SIMPLE_K3");
    }

    private void testTopCachePut() {
        cache.put("MIX_K1", "V1");
        cache.put("MIX_K2", null);
        Set s = new HashSet();
        s.add("MIX_K1");
        s.add("MIX_K2");
        s.add("MIX_K3");

        withCacheValueHolder(cache, s);
        withCacheValueHolder(l1Cache, s);
        withCacheValueHolder(l2Cache, s);

        cache.removeAll(s);
    }

    private void testSubCachePut() {
        l2Cache.put("MIX_K1", "V1");
        l2Cache.put("MIX_K2", null);
        Set s = new HashSet();
        s.add("MIX_K1");
        s.add("MIX_K2");
        s.add("MIX_K3");

        withCacheValueHolder(cache, s);
        withCacheValueHolder(l1Cache, s);
        withCacheValueHolder(l2Cache, s);

        cache.removeAll(s);
    }

    private void testCompatibilityBefore_2_5_0(){
        CacheValueHolder h1 = new CacheValueHolder("V1", System.currentTimeMillis() + 2000);
        CacheValueHolder h2 = new CacheValueHolder(null, System.currentTimeMillis() + 2000);
        l2Cache.put("MIX_K1", h1);
        l2Cache.put("MIX_K2", h2);

        Set s = new HashSet();
        s.add("MIX_K1");
        s.add("MIX_K2");
        s.add("MIX_K3");

        withCacheValueHolder(cache, s);
        withCacheValueHolder(l1Cache, s);
        withCacheValueHolderOfCacheValueHolder(l2Cache, s);

        cache.removeAll(s);
    }

    private void withCacheValueHolderOfCacheValueHolder(Cache c, Set s) {
        CacheGetResult<Object> r1 = c.GET("MIX_K1");
        CacheGetResult<Object> r2 = c.GET("MIX_K2");

        assertTrue(r1.getHolder().getValue() instanceof CacheValueHolder);
        assertTrue(r2.getHolder().getValue() instanceof CacheValueHolder);

        MultiGetResult<Object, Object> multiResult = c.GET_ALL(s);
        assertTrue(multiResult.isSuccess());
        assertTrue(multiResult.getValues().get("MIX_K1").isSuccess());
        assertEquals("V1", multiResult.getValues().get("MIX_K1").getValue());
        assertTrue(multiResult.getValues().get("MIX_K1").getHolder().getValue() instanceof CacheValueHolder);
        assertTrue(multiResult.getValues().get("MIX_K2").isSuccess());
        assertNull(multiResult.getValues().get("MIX_K2").getValue());
        assertTrue(multiResult.getValues().get("MIX_K2").getHolder().getValue() instanceof CacheValueHolder);
        assertFalse(multiResult.getValues().get("MIX_K3").isSuccess());
        assertNull(multiResult.getValues().get("MIX_K3").getValue());
        assertNull(multiResult.getValues().get("MIX_K3").getHolder());
    }

    private void withCacheValueHolder(Cache c, Set s) {
        CacheGetResult<Object> r1 = c.GET("MIX_K1");
        CacheGetResult<Object> r2 = c.GET("MIX_K2");

        assertTrue(r1.getHolder().getValue() instanceof String);
        assertNull(r2.getHolder().getValue());

        MultiGetResult<Object, Object> multiResult = c.GET_ALL(s);
        assertTrue(multiResult.isSuccess());
        assertTrue(multiResult.getValues().get("MIX_K1").isSuccess());
        assertEquals("V1", multiResult.getValues().get("MIX_K1").getValue());
        assertTrue(multiResult.getValues().get("MIX_K1").getHolder().getValue() instanceof String);
        assertTrue(multiResult.getValues().get("MIX_K2").isSuccess());
        assertNull(multiResult.getValues().get("MIX_K2").getValue());
        assertNull(multiResult.getValues().get("MIX_K2").getHolder().getValue());
        assertFalse(multiResult.getValues().get("MIX_K3").isSuccess());
        assertNull(multiResult.getValues().get("MIX_K3").getValue());
        assertNull(multiResult.getValues().get("MIX_K3").getHolder());
    }

}
