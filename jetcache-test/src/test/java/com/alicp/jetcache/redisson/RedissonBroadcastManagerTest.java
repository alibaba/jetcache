package com.alicp.jetcache.redisson;

import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.redis.AbstractBroadcastManagerTest;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.redisson.config.Config;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 2022/7/15.
 *
 * @author <a href="mailto:jeason1914@qq.com">yangyong</a>
 */
public class RedissonBroadcastManagerTest extends AbstractBroadcastManagerTest {
    private RedissonClient client;

    @Before
    public void initRedissonClient() {
        final Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        this.client = Redisson.create(config);
    }

    @After
    public void closeRedissonClient() {
        this.client.shutdown();
    }

    @Test
    public void redissonTopicTest() throws InterruptedException {
        RTopic topic1 = this.client.getTopic("topic", LongCodec.INSTANCE);
        AtomicBoolean stringMessageReceived = new AtomicBoolean();
        final CountDownLatch latch = new CountDownLatch(1);
        topic1.addListener(Long.class, (channel, msg) -> {
            Assertions.assertEquals(msg, 123);
            stringMessageReceived.set(true);
            latch.countDown();
        });
        topic1.publish(123L);
        latch.await();
        Assertions.assertTrue(stringMessageReceived.get());
    }

    @Test
    public void redissonTest() throws InterruptedException {
        final BroadcastManager bm = RedissonCacheBuilder.createBuilder()
                .redissonClient(client)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .createBroadcastManager(new SimpleCacheManager());
        testBroadcastManager(bm);
    }
}
