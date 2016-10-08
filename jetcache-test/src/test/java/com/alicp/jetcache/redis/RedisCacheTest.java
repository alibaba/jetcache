package com.alicp.jetcache.redis;

import com.alicp.jetcache.AbstractCacheTest;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.KryoValueDecoder;
import com.alicp.jetcache.support.KryoValueEncoder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisCacheTest extends AbstractCacheTest {

    @Test
    public void test() throws Exception {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPool pool = new JedisPool(pc, "localhost", 6379);
        cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .build();
        baseTest();
//        expireAfterWriteTest();

        cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .build();
        testKeyCoverter();

        int thread = 10;
        int count = 100;
        int time = 1000;
        cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix(new Random().nextInt() + "")
                .build();
        testConcurrentImpl(thread, count, time);
    }
}
