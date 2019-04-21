package com.alicp.jetcache.redis.springdata;

import com.alicp.jetcache.LoadingCacheTest;
import com.alicp.jetcache.RefreshCacheTest;
import com.alicp.jetcache.support.*;
import com.alicp.jetcache.test.external.AbstractExternalCacheTest;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringDataRedisCacheTest extends AbstractExternalCacheTest {

    @Test
    public void test() throws Exception {
        RedisConnectionFactory connectionFactory = new JedisConnectionFactory();

        cache = SpringDataRedisCacheBuilder.createBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + "")
                .expireAfterWrite(500, TimeUnit.MILLISECONDS)
                .buildCache();

        baseTest();
        fastjsonKeyCoverterTest();
        expireAfterWriteTest(cache.config().getExpireAfterWriteInMillis());

        LoadingCacheTest.loadingCacheTest(SpringDataRedisCacheBuilder.createBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + ""), 0);
        RefreshCacheTest.refreshCacheTest(SpringDataRedisCacheBuilder.createBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + ""), 200, 100);


        cache = SpringDataRedisCacheBuilder.createBuilder()
                .keyConvertor(null)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        nullKeyConvertorTest();

        int thread = 10;
        int time = 3000;
        cache = SpringDataRedisCacheBuilder.createBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        concurrentTest(thread, 500, time);
    }


}
