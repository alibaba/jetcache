package com.alicp.jetcache.anno.config;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created on 2016/11/16.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AnnoConfigTest.A.class)
public class AnnoConfigTest extends SpringTest {
    @Test
    public void test() throws Exception {
        doTest();
    }

    @Configuration
    @ComponentScan(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
    @EnableMethodCache(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.inittestbeans"})
    @EnableCreateCacheAnnotation
    public static class A {

        @Bean
        public SpringConfigProvider springConfigProvider() {
            return new SpringConfigProvider();
        }

        @Bean
        public GlobalCacheConfig config() {
            GlobalCacheConfig pc = TestUtil.createGloableConfig();
            return pc;
        }

        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }
    }


}
