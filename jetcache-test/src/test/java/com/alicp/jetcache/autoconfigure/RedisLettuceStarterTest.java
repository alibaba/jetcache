package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheTest;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheConfig;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.reactive.RedisClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
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
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableMethodCache(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableCreateCacheAnnotation
public class RedisLettuceStarterTest extends SpringTest {

    @Test
    public void tests() throws Exception {
        if (RedisLettuceCacheTest.checkOS()) {
            System.setProperty("spring.profiles.active", "redislettuce-cluster");
        } else {
            System.setProperty("spring.profiles.active", "redislettuce");
        }
        context = SpringApplication.run(RedisLettuceStarterTest.class);
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
        Assert.assertTrue(new LettuceFactory(acb, key, StatefulRedisConnection.class).getObject() instanceof StatefulRedisConnection);
        Assert.assertTrue(new LettuceFactory(acb, key, RedisCommands.class).getObject() instanceof RedisCommands);
        Assert.assertTrue(new LettuceFactory(acb, key, RedisAsyncCommands.class).getObject() instanceof RedisAsyncCommands);
        Assert.assertTrue(new LettuceFactory(acb, key, RedisReactiveCommands.class).getObject() instanceof RedisReactiveCommands);

        if (RedisLettuceCacheTest.checkOS()) {
            key = "remote.A2";
            Assert.assertTrue(new LettuceFactory(acb, key, RedisClusterClient.class).getObject() instanceof RedisClusterClient);
            Assert.assertTrue(new LettuceFactory(acb, key, RedisClusterCommands.class).getObject() instanceof RedisClusterCommands);
            Assert.assertTrue(new LettuceFactory(acb, key, RedisClusterAsyncCommands.class).getObject() instanceof RedisClusterAsyncCommands);
            Assert.assertTrue(new LettuceFactory(acb, key, RedisClusterReactiveCommands.class).getObject() instanceof RedisClusterReactiveCommands);

            key = "remote.A2_slave";
            Assert.assertTrue(new LettuceFactory(acb, key, RedisClusterClient.class).getObject() instanceof RedisClusterClient);
        }
    }

    @Component
    public static class A {
        @CreateCache
        private Cache c1;

        @CreateCache(area = "A1_slave")
        private Cache a1SlaveCache;

        public void test() throws Exception {
            Assert.assertNotNull(c1.unwrap(RedisClient.class));
            RedisLettuceCacheConfig cc1 = (RedisLettuceCacheConfig) c1.config();
            Assert.assertEquals(20000, cc1.getExpireAfterWriteInMillis());
            Assert.assertSame(FastjsonKeyConvertor.INSTANCE, cc1.getKeyConvertor());

            a1SlaveCache.put("K1", "V1");
            Thread.sleep(200);
            Assert.assertEquals("V1", a1SlaveCache.get("K1"));
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
        @DependsOn(RedisLettuceAutoConfiguration.AUTO_INIT_BEAN_NAME)
        public LettuceFactory defaultClient() {
            return new LettuceFactory("remote.default", RedisClient.class);
        }

        @Bean(name = "a1Client")
        @DependsOn(RedisLettuceAutoConfiguration.AUTO_INIT_BEAN_NAME)
        public LettuceFactory a1Client() {
            return new LettuceFactory("remote.A1", RedisClient.class);
        }
    }

}
