package com.alicp.jetcache.anno.filed;

import com.alicp.jetcache.AbstractCacheTest;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.ProxyCache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.TestUtil;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.config.SpringTest;
import com.alicp.jetcache.anno.config.beans.MyFactoryBean;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;

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
    @EnableMethodCache(basePackages = "com.alicp.jetcache.anno.config.beans")
    @EnableCreateCacheAnnotation
    public static class A {

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

        @Bean
        public Foo foo() {
            return new Foo();
        }

        public static class Foo extends AbstractCacheTest {
            @CreateCache
            private Cache cache;

            @CreateCache(area = "A1")
            private Cache cache_A1;

            @CreateCache(name = "name1")
            private Cache cacheSameName1;

            @CreateCache(name = "name2")
            private Cache cacheSameName2;

            @PostConstruct
            public void test() {
                super.cache = this.cache;
                super.baseTest();

                cache.put("K1", "V1");
                Assert.assertNull(cache_A1.get("K1"));

                Assert.assertEquals(((ProxyCache)cacheSameName1).getTargetCache(), ((ProxyCache)cacheSameName2).getTargetCache());
            }
        }
    }


}
