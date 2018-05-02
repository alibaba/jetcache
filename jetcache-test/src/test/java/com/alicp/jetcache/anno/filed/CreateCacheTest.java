package com.alicp.jetcache.anno.filed;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.*;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.test.MockRemoteCache;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.test.support.DynamicQueryWithEquals;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CreateCacheTest.A.class)
public class CreateCacheTest extends SpringTest {

    @Test
    public void test() throws Exception {
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
            private Cache cache1;

            @CreateCache
            private Cache cache2;

            @CreateCache(area = "A1")
            private Cache cache_A1;

            @CreateCache(name = "sameCacheName")
            private Cache cacheSameName1;

            @CreateCache(name = "sameCacheName")
            private Cache cacheSameName2;

            @CreateCache(area = "A1", name = "name1", expire = 50, timeUnit = TimeUnit.MILLISECONDS, cacheType = CacheType.BOTH, localLimit = 10, serialPolicy = SerialPolicy.JAVA, keyConvertor = KeyConvertor.NONE)
            private Cache cacheWithConfig;

            @CreateCache(cacheType =CacheType.LOCAL, keyConvertor = KeyConvertor.NONE)
            private Cache cacheWithoutConvertor;

            @CreateCache
            @CacheRefresh(timeUnit = TimeUnit.MILLISECONDS, refresh = 100)
            private Cache cacheWithRefresh;

            @CreateCache
            @CachePenetrationProtect
            private Cache cacheWithProtect;

            @CreateCache(expire = 2, localExpire = 1, cacheType = CacheType.BOTH)
            private Cache cacheWithLocalExpire_1;
            @CreateCache(expire = 2, cacheType = CacheType.BOTH)
            private Cache cacheWithLocalExpire_2;
            @CreateCache(expire = 2, localExpire = 1, cacheType = CacheType.LOCAL)
            private Cache cacheWithLocalExpire_3;


            private Cache getTarget(Cache cache) {
                while(cache instanceof ProxyCache){
                    cache = ((ProxyCache) cache).getTargetCache();
                }
                return cache;
            }

            @PostConstruct
            public void test() throws Exception {
                runGeneralTest();
                refreshTest();
                cacheWithoutConvertorTest();
                AbstractCacheTest.penetrationProtectTest(cacheWithProtect);
                testCacheWithLocalExpire();

                cache1.put("KK1", "V1");
                Assert.assertNull(cache_A1.get("KK1"));
                Assert.assertNull(cache2.get("KK1"));

                Assert.assertSame(getTarget(cacheSameName1), getTarget(cacheSameName2));
                Assert.assertNotSame(getTarget(cacheSameName1), getTarget(cache1));

                cacheSameName1.put("SameKey", "SameValue");
                Assert.assertEquals(cacheSameName1.get("SameKey"),cacheSameName2.get("SameKey"));
                Assert.assertNull(cache1.get("SameKey"));

                Assert.assertTrue(getTarget(cache1) instanceof MockRemoteCache);
                Assert.assertSame(FastjsonKeyConvertor.INSTANCE, cache1.config().getKeyConvertor());

                Assert.assertTrue(getTarget(cacheWithConfig) instanceof MultiLevelCache);
                Assert.assertEquals(50, cacheWithConfig.config().getExpireAfterWriteInMillis());

                MultiLevelCache mc = (MultiLevelCache) getTarget(cacheWithConfig);
                Cache localCache = getTarget(mc.caches()[0]);
                Cache remoteCache = getTarget(mc.caches()[1]);
                Assert.assertTrue(localCache instanceof LinkedHashMapCache);
                Assert.assertTrue(remoteCache instanceof MockRemoteCache);
                EmbeddedCacheConfig localConfig = (EmbeddedCacheConfig) localCache.config();
                ExternalCacheConfig remoteConfig = (ExternalCacheConfig) remoteCache.config();
                Assert.assertEquals(50, localConfig.getExpireAfterWriteInMillis());
                Assert.assertEquals(50, remoteConfig.getExpireAfterWriteInMillis());
                Assert.assertEquals(10, localConfig.getLimit());
                Assert.assertEquals(JavaValueEncoder.class, remoteConfig.getValueEncoder().getClass());
                Assert.assertEquals(JavaValueDecoder.class, remoteConfig.getValueDecoder().getClass());
                Assert.assertNull(localConfig.getKeyConvertor());
                Assert.assertNull(remoteConfig.getKeyConvertor());

            }

            private void testCacheWithLocalExpire() {
                MultiLevelCacheConfig<?,?> config = (MultiLevelCacheConfig) cacheWithLocalExpire_1.config();
                Assert.assertTrue(config.isUseExpireOfSubCache());
                Assert.assertEquals(2000, config.getExpireAfterWriteInMillis());
                Assert.assertEquals(1000, config.getCaches().get(0).config().getExpireAfterWriteInMillis());
                Assert.assertEquals(2000, config.getCaches().get(1).config().getExpireAfterWriteInMillis());

                config = (MultiLevelCacheConfig) cacheWithLocalExpire_2.config();
                Assert.assertFalse(config.isUseExpireOfSubCache());
                Assert.assertEquals(2000, config.getExpireAfterWriteInMillis());
                Assert.assertEquals(2000, config.getCaches().get(0).config().getExpireAfterWriteInMillis());
                Assert.assertEquals(2000, config.getCaches().get(1).config().getExpireAfterWriteInMillis());

                Assert.assertEquals(2000, cacheWithLocalExpire_3.config().getExpireAfterWriteInMillis());
            }

            private void runGeneralTest() throws Exception {
                super.cache = this.cache1;
                super.baseTest();
                LoadingCacheTest.loadingCacheTest(cache1, 0);
                RefreshCacheTest.refreshCacheTest(cache1, 200, 100);
            }

            private void cacheWithoutConvertorTest() {
                DynamicQuery q1 = new DynamicQuery();
                q1.setId(1000);
                q1.setName("N1");
                DynamicQuery q2 = new DynamicQuery();
                q2.setId(1000);
                q2.setName("N2");
                DynamicQuery q3 = new DynamicQuery();
                q3.setId(1000);
                q3.setName("N1");
                cacheWithoutConvertor.put(q1, "V");
                Assert.assertEquals(CacheResultCode.NOT_EXISTS, cacheWithoutConvertor.GET(q2).getResultCode());
                Assert.assertEquals(CacheResultCode.NOT_EXISTS, cacheWithoutConvertor.GET(q3).getResultCode());

                DynamicQueryWithEquals dqwe1 = new DynamicQueryWithEquals();
                dqwe1.setId(1000);
                dqwe1.setName("N1");
                DynamicQueryWithEquals dqwe2 = new DynamicQueryWithEquals();
                dqwe2.setId(1001);
                dqwe2.setName("N2");
                DynamicQueryWithEquals dqwe3 = new DynamicQueryWithEquals();
                dqwe3.setId(1000);
                dqwe3.setName("N1");
                cacheWithoutConvertor.put(dqwe1, "V");
                Assert.assertEquals(CacheResultCode.NOT_EXISTS, cacheWithoutConvertor.GET(dqwe2).getResultCode());
                Assert.assertEquals(CacheResultCode.SUCCESS, cacheWithoutConvertor.GET(dqwe3).getResultCode());
            }

            private int refreshCount;
            private void refreshTest() throws Exception {
                cacheWithRefresh.config().setLoader((k) -> refreshCount++);
                cacheWithRefresh.put("K1", "V1");
                Assert.assertEquals("V1", cacheWithRefresh.get("K1"));
                Thread.sleep((long) (cacheWithRefresh.config().getRefreshPolicy().getRefreshMillis() * 1.5));
                Assert.assertEquals(0, cacheWithRefresh.get("K1"));
                cacheWithRefresh.close();
            }
        }
    }


}
