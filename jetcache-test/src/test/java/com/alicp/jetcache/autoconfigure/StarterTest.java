package com.alicp.jetcache.autoconfigure;

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
import redis.clients.jedis.JedisPool;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("com.alicp.jetcache.test.beans")
@EnableMethodCache(basePackages = "com.alicp.jetcache.test.beans")
public class StarterTest extends SpringTest {

    @Test
    public void tests() {
        context = SpringApplication.run(StarterTest.class);
        doTest();

        JedisPool t1 = (JedisPool) context.getBean("defaultPool");
        JedisPool t2 = (JedisPool) context.getBean("A1Pool");
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
        Assert.assertNotSame(t1 , t2);
    }

    @Configuration
    public static class A {
        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }
        @Bean
        public JedisPoolFactory defaultPool() {
            return new JedisPoolFactory("remote.default");
        }
        @Bean
        public JedisPoolFactory A1Pool() {
            return new JedisPoolFactory("remote.A1");
        }
    }

}
