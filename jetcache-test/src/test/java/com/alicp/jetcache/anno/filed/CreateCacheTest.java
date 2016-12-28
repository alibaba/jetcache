package com.alicp.jetcache.anno.filed;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.*;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.test.anno.MockRemoteCache;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTest;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
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
    @ComponentScan(basePackages = "com.alicp.jetcache.test.beans")
    @EnableMethodCache(basePackages = "com.alicp.jetcache.test.beans")
    @EnableCreateCacheAnnotation
    public static class A {

        @Bean
        public SpringConfigProvider springConfigProvider() {
            return new SpringConfigProvider();
        }

        @Bean
        public GlobalCacheConfig config(SpringConfigProvider configProvider) {
            GlobalCacheConfig pc = TestUtil.createGloableConfig(configProvider);
            return pc;
        }

        @Bean(name = "factoryBeanTarget")
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

            @CreateCache(name = "sameCacheName")
            private Cache cacheSameName1;

            @CreateCache(name = "sameCacheName")
            private Cache cacheSameName2;

            @CreateCache(area = "A1", name = "name1", expire = 5, cacheType = CacheType.BOTH, localLimit = 10, serialPolicy = SerialPolicy.JAVA, keyConvertor = KeyConvertor.NONE)
            private Cache cacheWithConfig;

            private Cache getTarget(Cache cache) {
                while(cache instanceof ProxyCache){
                    cache = ((ProxyCache) cache).getTargetCache();
                }
                return cache;
            }

            @PostConstruct
            public void test() throws Exception {
                super.cache = this.cache;
                super.baseTest();

                cache.put("K1", "V1");
                Assert.assertNull(cache_A1.get("K1"));

                Assert.assertSame(getTarget(cacheSameName1), getTarget(cacheSameName2));
                Assert.assertNotSame(getTarget(cacheSameName1), getTarget(cache));

                Assert.assertTrue(getTarget(cache) instanceof MockRemoteCache);
                Assert.assertSame(FastjsonKeyConvertor.INSTANCE, cache.config().getKeyConvertor());

                Assert.assertTrue(getTarget(cacheWithConfig) instanceof MultiLevelCache);
                MultiLevelCache mc = (MultiLevelCache) getTarget(cacheWithConfig);
                Cache localCache = getTarget(mc.caches()[0]);
                Cache remoteCache = getTarget(mc.caches()[1]);
                Assert.assertTrue(localCache instanceof LinkedHashMapCache);
                Assert.assertTrue(remoteCache instanceof MockRemoteCache);
                EmbeddedCacheConfig localConfig = (EmbeddedCacheConfig) localCache.config();
                ExternalCacheConfig remoteConfig = (ExternalCacheConfig) remoteCache.config();
                Assert.assertEquals(5000, localConfig.getDefaultExpireInMillis());
                Assert.assertEquals(5000, remoteConfig.getDefaultExpireInMillis());
                Assert.assertEquals(10, localConfig.getLimit());
                Assert.assertSame(JavaValueEncoder.INSTANCE, remoteConfig.getValueEncoder());
                Assert.assertSame(JavaValueDecoder.INSTANCE, remoteConfig.getValueDecoder());
                Assert.assertNull(localConfig.getKeyConvertor());
                Assert.assertNull(remoteConfig.getKeyConvertor());
            }
        }
    }


}
