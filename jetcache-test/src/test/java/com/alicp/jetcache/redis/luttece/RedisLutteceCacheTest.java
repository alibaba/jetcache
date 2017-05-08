package com.alicp.jetcache.redis.luttece;

import com.alicp.jetcache.support.*;
import com.alicp.jetcache.test.external.AbstractExternalCacheTest;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.RedisClusterClient;
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
                .withSentinel("127.0.0.1, 26381")
                .build();
        RedisClient client = RedisClient.create(redisUri);
        test(client);
    }

//    @Test
    public void testCluster() throws Exception {
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
        expireAfterWriteTest(cache.config().getDefaultExpireInMillis());
        fastjsonKeyCoverterTest();

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
        concurrentTest(thread, 500 , time);
    }
}
