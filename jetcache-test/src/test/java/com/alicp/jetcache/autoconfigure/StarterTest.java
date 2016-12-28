package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.test.spring.SpringTest;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
    }

    @Configuration
    public static class A {
        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }

    }

}
