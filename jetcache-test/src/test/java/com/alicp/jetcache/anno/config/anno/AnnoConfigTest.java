package com.alicp.jetcache.anno.config.anno;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.TestUtil;
import com.alicp.jetcache.anno.config.EnableJetCache;
import com.alicp.jetcache.anno.config.SpringTest;
import com.alicp.jetcache.anno.config.beans.MyFactoryBean;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringGlobalCacheConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created on 2016/11/16.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AnnoConfigTest.A.class)
public class AnnoConfigTest extends SpringTest {
    @Test
    public void test() {
        doTest();
    }

    @Configuration
    @ComponentScan(basePackages = "com.alicp.jetcache.anno.config.beans")
    @EnableJetCache(basePackages = "com.alicp.jetcache.anno.config.beans")
    public static class A {

        @Bean
        public GlobalCacheConfig config(){
            GlobalCacheConfig pc = TestUtil.createGloableConfig(SpringGlobalCacheConfig::new);
            return pc;
        }

        @Bean("factoryBeanTarget")
        public MyFactoryBean factoryBean(){
            return new MyFactoryBean();
        }
    }


}
