package com.alicp.jetcache.redis.luttece;

import com.alicp.jetcache.LoadingCacheTest;
import com.alicp.jetcache.RefreshCacheTest;
import com.alicp.jetcache.support.*;
import com.alicp.jetcache.test.external.AbstractExternalCacheTest;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.rx.RedisReactiveCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.async.RedisClusterAsyncCommands;
import com.lambdaworks.redis.cluster.api.rx.RedisClusterReactiveCommands;
import com.lambdaworks.redis.cluster.api.sync.RedisClusterCommands;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/5/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisLutteceCacheTest extends AbstractExternalCacheTest {
    public static boolean checkOS() {
        String os = System.getProperty("os.name");
        if (os.contains("Mac") || os.contains("Windows")) {
            // redis cluster must run with --net=host, but this can't work in Docker for Mac
            return false;
        } else {
            return true;
        }
    }

    @Test
    public void testSimple() throws Exception {
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        test(client);
    }

    @Test
    public void testSentinel() throws Exception {
        RedisURI redisUri = RedisURI.Builder
                .sentinel("127.0.0.1", 26379, "mymaster")
                .withSentinel("127.0.0.1", 26380)
                .withSentinel("127.0.0.1", 26381)
                .build();
        RedisClient client = RedisClient.create(redisUri);
        test(client);
    }

    @Test
    public void testCluster() throws Exception {
        if (!checkOS()) {
            return;
        }
        RedisURI node1 = RedisURI.create("127.0.0.1", 7000);
        RedisURI node2 = RedisURI.create("127.0.0.1", 7001);
        RedisURI node3 = RedisURI.create("127.0.0.1", 7002);
        RedisClusterClient client = RedisClusterClient.create(Arrays.asList(node1, node2, node3));
        test(client);
    }

    private void test(AbstractRedisClient client) throws Exception {
        cache = RedisLutteceCacheBuilder.createRedisLutteceCacheBuilder()
                .redisClient(client)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .buildCache();
        baseTest();
        expireAfterWriteTest(cache.config().getExpireAfterWriteInMillis());
        fastjsonKeyCoverterTest();
        testUnwrap(client);

        LoadingCacheTest.loadingCacheTest(RedisLutteceCacheBuilder.createRedisLutteceCacheBuilder()
                .redisClient(client)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(200, TimeUnit.MILLISECONDS), 50);

        cache = RedisLutteceCacheBuilder.createRedisLutteceCacheBuilder()
                .redisClient(client)
                .keyConvertor(null)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        nullKeyConvertorTest();

        int thread = 10;
        int time = 3000;
        cache = RedisLutteceCacheBuilder.createRedisLutteceCacheBuilder()
                .redisClient(client)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        concurrentTest(thread, 500, time);
    }

    private void testUnwrap(AbstractRedisClient client) {
        Assert.assertTrue(cache.unwrap(AbstractRedisClient.class) instanceof AbstractRedisClient);
        if (client instanceof RedisClient) {
            Assert.assertTrue(cache.unwrap(RedisClient.class) instanceof RedisClient);
            Assert.assertTrue(cache.unwrap(RedisCommands.class) instanceof RedisCommands);
            Assert.assertTrue(cache.unwrap(RedisAsyncCommands.class) instanceof RedisAsyncCommands);
            Assert.assertTrue(cache.unwrap(RedisReactiveCommands.class) instanceof RedisReactiveCommands);
        } else {
            Assert.assertTrue(cache.unwrap(RedisClusterClient.class) instanceof RedisClusterClient);
            Assert.assertTrue(cache.unwrap(RedisClusterCommands.class) instanceof RedisClusterCommands);
            Assert.assertTrue(cache.unwrap(RedisClusterAsyncCommands.class) instanceof RedisClusterAsyncCommands);
            Assert.assertTrue(cache.unwrap(RedisClusterReactiveCommands.class) instanceof RedisClusterReactiveCommands);
        }
    }
}
