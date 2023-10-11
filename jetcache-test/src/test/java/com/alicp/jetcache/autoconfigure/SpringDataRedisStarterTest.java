package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 2019/5/1.
 *
 * @author huangli
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableMethodCache(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
@EnableCreateCacheAnnotation
public class SpringDataRedisStarterTest extends SpringTest {

    @Test
    public void tests() throws Exception {
        System.setProperty("spring.profiles.active", "redis-springdata");
        context = SpringApplication.run(SpringDataRedisStarterTest.class);
        doTest();
    }

    @Component
    public static class A {
        @CreateCache
        private Cache cache;

        @PostConstruct
        public void test() {
            Assert.assertTrue(cache.PUT("K", "V").isSuccess());
        }
    }

    @Bean(name = "factoryBeanTarget")
    public MyFactoryBean factoryBean() {
        return new MyFactoryBean();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory connectionFactory =  new LettuceConnectionFactory(
                new RedisStandaloneConfiguration("127.0.0.1", 6379));
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }
}
