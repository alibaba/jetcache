package com.alicp.jetcache.anno.filed;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.TestUtil;
import com.alicp.jetcache.anno.config.EnableJetCache;
import com.alicp.jetcache.anno.config.SpringTest;
import com.alicp.jetcache.anno.config.beans.MyFactoryBean;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CreateCacheTest.A.class)
public class CreateCacheTest extends SpringTest {


    @Test
    public void test() {
        doTest();
    }


    @Configuration
    @ComponentScan(basePackages = "com.alicp.jetcache.anno.config.beans")
    @EnableJetCache(basePackages = "com.alicp.jetcache.anno.config.beans")
    public static class A {

        @Bean
        public Foo foo() {
            return new Foo();
        }

        public static class Foo {
            @CreateCache
            private Cache cache;
        }

        @Bean
        public SpringConfigProvider springConfigProvider() {
            return new SpringConfigProvider();
        }

        @Bean
        public GlobalCacheConfig config(@Autowired SpringConfigProvider configProvider) {
            GlobalCacheConfig pc = TestUtil.createGloableConfig(configProvider);
            return pc;
        }

        @Bean("factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }
    }


}
