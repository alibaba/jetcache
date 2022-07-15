package com.alicp.jetcache.redisson;

import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.redis.AbstractBroadcastManagerTest;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Random;

/**
 * Created on 2022/7/15.
 *
 * @author <a href="mailto:jeason1914@qq.com">yangyong</a>
 */
public class RedissonBroadcastManagerTest extends AbstractBroadcastManagerTest {

    @Test
    public void redissonTest() throws Exception {
        final Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        doTest(Redisson.create(config));
    }

    private void doTest(final RedissonClient client) throws InterruptedException {
        final BroadcastManager bm = RedissonCacheBuilder.createBuilder()
                .redissonClient(client)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .createBroadcastManager(new SimpleCacheManager());
        //testBroadcastManager(bm);
    }
}
