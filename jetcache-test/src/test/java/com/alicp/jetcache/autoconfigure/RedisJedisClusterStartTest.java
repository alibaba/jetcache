package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.redis.RedisCacheConfig;
import com.alicp.jetcache.redis.jedis.JedisClusterCacheConfig;
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
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author eason.feng at 2019/12/12/0012 17:25
 **/
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableMethodCache(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableCreateCacheAnnotation
public class RedisJedisClusterStartTest extends SpringTest {

    @Test
    public void test() throws Exception {
        System.setProperty("spring.profiles.active", "rediscluster");
        context = SpringApplication.run(RedisJedisClusterStartTest.class);
        A bean = context.getBean(A.class);
        bean.test();
    }

    @Component
    public static class A {

        @CreateCache(cacheType = CacheType.LOCAL)
        private Cache c1;

        @CreateCache(cacheType = CacheType.REMOTE, area = "A1")
        private Cache c2;

        @CreateCache(cacheType = CacheType.REMOTE, area = "default")
        private Cache<String, String> c3;

        @Cached(cacheType = CacheType.REMOTE, area = "default", expire = 20)
        public String getId(String id) {
            return id;
        }

        public void test() {
            Assert.assertNotNull(c1.unwrap(com.github.benmanes.caffeine.cache.Cache.class));

            RedisCacheConfig c = (RedisCacheConfig) c2.config();
            Assert.assertNotNull(c);
            Map<String, String> map = new HashMap<>();
            map.put("a", "a");
            map.put("#1", "a");
            map.put("*1", "a");
            map.put("---!===", "a");
            map.put("mmmm", "a");
            CacheResult result = c2.PUT_ALL(map);
            Assert.assertTrue(result.getResultCode() == CacheResultCode.SUCCESS);
            JedisClusterCacheConfig c3Config = (JedisClusterCacheConfig) c3.config();
            Assert.assertNotNull(c3Config);
            result = c3.PUT_ALL(map);
            Assert.assertTrue(result.getResultCode() == CacheResultCode.SUCCESS);
            String a = c3.get("a");
            Assert.assertEquals(a, map.get("a"));

            getId("id");
            Assert.assertNotNull(getId("id"));
        }
    }

    @Component
    public static class B {
        @Autowired
        private JedisCluster defaultCluster;

        @Autowired
        private JedisCluster a1CLUSTER;

        @PostConstruct
        public void init() {
            Assert.assertNotNull(defaultCluster);
//            Assert.assertNotNull(a1CLUSTER);
        }
    }

    @Configuration
    public static class Config {

        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }

        @Bean(name = "defaultCluster")
        @DependsOn(RedisAutoConfiguration.AUTO_INIT_BEAN_NAME)
        public JedisClusterFactory defaultCluster() {
            return new JedisClusterFactory("remote.default", JedisCluster.class);
        }

    }

}
