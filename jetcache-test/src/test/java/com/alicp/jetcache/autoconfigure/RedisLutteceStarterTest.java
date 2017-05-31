package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.redis.luttece.RedisLutteceCacheConfig;
import com.alicp.jetcache.redis.luttece.RedisLutteceCacheTest;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.rx.RedisReactiveCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.async.RedisClusterAsyncCommands;
import com.lambdaworks.redis.cluster.api.rx.RedisClusterReactiveCommands;
import com.lambdaworks.redis.cluster.api.sync.RedisClusterCommands;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 2017/05/11.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableMethodCache(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableCreateCacheAnnotation
public class RedisLutteceStarterTest extends SpringTest {

    @Test
    public void tests() throws Exception {
        if (RedisLutteceCacheTest.checkOS()) {
            System.setProperty("spring.profiles.active", "redisluttece-cluster");
        } else {
            System.setProperty("spring.profiles.active", "redisluttece");
        }
        context = SpringApplication.run(RedisLutteceStarterTest.class);
        doTest();
        A bean = context.getBean(A.class);
        bean.test();

        RedisClient t1 = (RedisClient) context.getBean("defaultClient");
        RedisClient t2 = (RedisClient) context.getBean("a1Client");
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
        Assert.assertNotSame(t1, t2);

        AutoConfigureBeans acb = context.getBean(AutoConfigureBeans.class);

        String key = "remote.A1";
        Assert.assertTrue(new LutteceFactory(acb, key, StatefulRedisConnection.class).getObject() instanceof StatefulRedisConnection);
        Assert.assertTrue(new LutteceFactory(acb, key, RedisCommands.class).getObject() instanceof RedisCommands);
        Assert.assertTrue(new LutteceFactory(acb, key, RedisAsyncCommands.class).getObject() instanceof RedisAsyncCommands);
        Assert.assertTrue(new LutteceFactory(acb, key, RedisReactiveCommands.class).getObject() instanceof RedisReactiveCommands);

        if (RedisLutteceCacheTest.checkOS()) {
            key = "remote.A2";
            Assert.assertTrue(new LutteceFactory(acb, key , RedisClusterClient.class).getObject() instanceof RedisClusterClient);
            Assert.assertTrue(new LutteceFactory(acb, key , RedisClusterCommands.class).getObject() instanceof RedisClusterCommands);
            Assert.assertTrue(new LutteceFactory(acb, key , RedisClusterAsyncCommands.class).getObject() instanceof RedisClusterAsyncCommands);
            Assert.assertTrue(new LutteceFactory(acb, key , RedisClusterReactiveCommands.class).getObject() instanceof RedisClusterReactiveCommands);
        }
    }

    @Component
    public static class A {
        @CreateCache
        private Cache c1;

        public void test() {
            Assert.assertNotNull(c1.unwrap(RedisClient.class));
            RedisLutteceCacheConfig cc1 = (RedisLutteceCacheConfig) c1.config();
            Assert.assertEquals(20000, cc1.getExpireAfterWriteInMillis());
            Assert.assertNull(cc1.getKeyConvertor());
        }
    }

    @Component
    public static class B {
        @Autowired
        private RedisClient defaultClient;

        @Autowired
        private RedisClient a1Client;

        @PostConstruct
        public void init() {
            Assert.assertNotNull(defaultClient);
            Assert.assertNotNull(a1Client);
        }
    }

    @Configuration
    public static class Config {
        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }

        @Bean(name = "defaultClient")
        @DependsOn(RedisLutteceAutoConfiguration.AUTO_INIT_BEAN_NAME)
        public LutteceFactory defaultClient() {
            return new LutteceFactory("remote.default", RedisClient.class);
        }

        @Bean(name = "a1Client")
        @DependsOn(RedisLutteceAutoConfiguration.AUTO_INIT_BEAN_NAME)
        public LutteceFactory a1Client() {
            return new LutteceFactory("remote.A1", RedisClient.class);
        }
    }

}
