package com.alicp.jetcache.redis;

import com.alicp.jetcache.LoadingCache;
import com.alicp.jetcache.LoadingCacheTest;
import com.alicp.jetcache.RefreshCacheTest;
import com.alicp.jetcache.support.*;
import com.alicp.jetcache.test.external.AbstractExternalCacheTest;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisCacheTest extends AbstractExternalCacheTest {

    @Test
    public void testSimplePool() throws Exception {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPool pool = new JedisPool(pc, "localhost", 6379);

        testWithPool(pool);
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

        testWithPool(pool);
    }

    private void testWithPool(Pool<Jedis> pool) throws Exception {
        cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .buildCache();
        baseTest();
        fastjsonKeyCoverterTest();
        expireAfterWriteTest(cache.config().getExpireAfterWriteInMillis());

        LoadingCacheTest.loadingCacheTest(RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(200, TimeUnit.MILLISECONDS), 0);
        RefreshCacheTest.refreshCacheTest(RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(200, TimeUnit.MILLISECONDS));


        cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(null)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        nullKeyConvertorTest();

        int thread = 10;
        int time = 3000;
        cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        concurrentTest(thread, 500 , time);
    }
}
