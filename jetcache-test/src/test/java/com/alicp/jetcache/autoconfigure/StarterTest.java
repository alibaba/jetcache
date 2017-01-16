package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
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
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("com.alicp.jetcache.test.beans")
@EnableMethodCache(basePackages = "com.alicp.jetcache.test.beans")
@EnableCreateCacheAnnotation
public class StarterTest extends SpringTest {

    @Test
    public void tests() {
        context = SpringApplication.run(StarterTest.class);
        doTest();
        A bean = context.getBean(A.class);
//        bean.test();

        JedisPool t1 = (JedisPool) context.getBean("defaultPool");
        JedisPool t2 = (JedisPool) context.getBean("A1Pool");
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
        Assert.assertNotSame(t1, t2);
    }

    @Component
    public static class A {
        @CreateCache(cacheType = CacheType.LOCAL)
        private Cache c1;

        public void test() {
            Assert.assertNotNull(c1.unwrap(com.github.benmanes.caffeine.cache.Cache.class));
            EmbeddedCacheConfig cc1 = (EmbeddedCacheConfig) c1.config();
            Assert.assertEquals(200, cc1.getLimit());
            Assert.assertEquals(300, cc1.getDefaultExpireInMillis());
            Assert.assertFalse(cc1.isExpireAfterAccess());
            Assert.assertNull(cc1.getKeyConvertor());
        }
    }

    @Component
    public static class B {
        @Autowired
        private JedisPool defaultPool;

        @Autowired
        private JedisPool A1Pool;

        @PostConstruct
        public void init() {
            Assert.assertNotNull(defaultPool);
            Assert.assertNotNull(A1Pool);
        }
    }

    @Configuration
    public static class Config {
        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }

        @Bean(name = "defaultPool")
        @DependsOn("redisAutoInit")
        public JedisPoolFactory defaultPool() {
            return new JedisPoolFactory("remote.default", JedisPool.class);
        }

        @Bean(name = "A1Pool")
        @DependsOn("redisAutoInit")
        public JedisPoolFactory A1Pool() {
            return new JedisPoolFactory("remote.A1", JedisPool.class);
        }
    }

}
