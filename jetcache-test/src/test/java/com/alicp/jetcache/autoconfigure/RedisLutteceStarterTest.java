package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.redis.luttece.RedisLutteceCacheConfig;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import com.lambdaworks.redis.RedisClient;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

import javax.annotation.PostConstruct;

/**
 * Created on 2016/11/23.
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
    public void tests() {
        System.setProperty("spring.profiles.active", "redisluttece");
        context = SpringApplication.run(RedisLutteceStarterTest.class);
        doTest();
        A bean = context.getBean(A.class);
        bean.test();

        RedisClient t1 = (RedisClient) context.getBean("defaultClient");
        RedisClient t2 = (RedisClient) context.getBean("a1Client");
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
        Assert.assertNotSame(t1, t2);
    }

    @Component
    public static class A {
        @CreateCache
        private Cache c1;

        public void test() {
            Assert.assertNotNull(c1.unwrap(RedisClient.class));
            RedisLutteceCacheConfig cc1 = (RedisLutteceCacheConfig) c1.config();
            Assert.assertEquals(20000, cc1.getDefaultExpireInMillis());
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
