package com.alicp.jetcache.anno.config;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.template.QuickConfig;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * Created on 2022/08/02.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConfigTest2.A.class)
public class ConfigTest2 {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void test() {
        Cache c1 = cacheManager.getCache("cache1");
        Assertions.assertEquals(1000, c1.config().getExpireAfterWriteInMillis());
        Bean3 b3 = context.getBean(Bean3.class);
        b3.useCache2();
        Cache c2 = cacheManager.getCache("cache2");
        Assertions.assertEquals(1000, c2.config().getExpireAfterWriteInMillis());
    }


    @Configuration
    @EnableMethodCache(basePackages = "com.alicp.jetcache.anno.config.ConfigTest2")
    @EnableCreateCacheAnnotation
    @Import(JetCacheBaseBeans.class)
    public static class A {
        @Bean
        public Bean1 bean1() {
            return new Bean1();
        }

        @Bean
        public Bean2 bean2() {
            return new Bean2();
        }

        @Bean
        public Bean3 bean3() {
            return new Bean3();
        }

        @Bean
        public GlobalCacheConfig config() {
            return TestUtil.createGloableConfig();
        }

    }

    public static class Bean1 {

        @Autowired
        private CacheManager cacheManager;

        private Cache cache1;

        @PostConstruct
        public void init() {
            cache1 = cacheManager.getOrCreateCache(QuickConfig.newBuilder("cache1")
                    .expire(Duration.ofSeconds(1)).build());
        }

        @Cached(name = "cache1", expire = 2)
        public String useCache1() {
            return null;
        }
    }

    public static class Bean2 {
        @Autowired
        private CacheManager cacheManager;
        private Cache cache2;

        @PostConstruct
        public void init() {
            cache2 = cacheManager.getOrCreateCache(QuickConfig.newBuilder("cache2")
                    .expire(Duration.ofSeconds(1)).build());
        }
    }

    public static class Bean3 {
        @Cached(name = "cache2", expire = 2)
        public String useCache2() {
            return null;
        }
    }
}
