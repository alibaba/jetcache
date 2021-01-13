package com.alicp.jetcache.redis.springdata;

import com.alicp.jetcache.LoadingCacheTest;
import com.alicp.jetcache.RefreshCacheTest;
import com.alicp.jetcache.support.*;
import com.alicp.jetcache.test.external.AbstractExternalCacheTest;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisSpringDataCacheTest extends AbstractExternalCacheTest {

    @Test
    public void jedisTest() throws Exception {
        RedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        doTest(connectionFactory);
    }

    @Test
    public void lettuceTest() throws Exception {
        LettuceConnectionFactory connectionFactory =  new LettuceConnectionFactory(
                new RedisStandaloneConfiguration("127.0.0.1", 6379));
        connectionFactory.afterPropertiesSet();
        doTest(connectionFactory);
    }


    private void doTest(RedisConnectionFactory connectionFactory) throws Exception {


        cache = RedisSpringDataCacheBuilder.createBuilder()
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

        LoadingCacheTest.loadingCacheTest(RedisSpringDataCacheBuilder.createBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + ""), 0);
        RefreshCacheTest.refreshCacheTest(RedisSpringDataCacheBuilder.createBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + ""), 200, 100);


        cache = RedisSpringDataCacheBuilder.createBuilder()
                .keyConvertor(null)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        nullKeyConvertorTest();

        int thread = 10;
        int time = 3000;
        cache = RedisSpringDataCacheBuilder.createBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .connectionFactory(connectionFactory)
                .keyPrefix(new Random().nextInt() + "")
                .buildCache();
        concurrentTest(thread, 500, time);
    }


}
