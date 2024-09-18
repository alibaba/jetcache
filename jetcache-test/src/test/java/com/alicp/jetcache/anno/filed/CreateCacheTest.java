package com.alicp.jetcache.anno.filed;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.LoadingCacheTest;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.MultiLevelCacheConfig;
import com.alicp.jetcache.ProxyCache;
import com.alicp.jetcache.RefreshCacheTest;
import com.alicp.jetcache.anno.CachePenetrationProtect;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.KeyConvertor;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.external.MockRemoteCache;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.test.support.DynamicQueryWithEquals;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/9.
 *
 * @author huangli
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
    @Import(JetCacheBaseBeans.class)
    public static class A {

        @Bean
        public GlobalCacheConfig config() {
            GlobalCacheConfig pc = TestUtil.createGloableConfig();
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

            @Autowired
            private ConfigProvider configProvider;

            @Autowired
            private CacheManager cacheManager;

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
            private Cache cacheWithRefresh1;

            @CreateCache
            @CacheRefresh(timeUnit = TimeUnit.MILLISECONDS, refresh = 100)
            private Cache cacheWithRefresh2;

            @CreateCache
            @CacheRefresh(timeUnit = TimeUnit.MILLISECONDS, refresh = 100)
            private Cache cacheWithRefresh3;

            @CreateCache
            @CachePenetrationProtect(timeout = 1)
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
            public void init() throws Exception {
                runGeneralTest();
                refreshTest();
                cacheWithoutConvertorTest();
                testCacheWithLocalExpire();
            }

            private void runGeneralTest() throws Exception {
                super.cache = this.cache1;
                super.baseTest();
            }
            private int refreshCount;
            private void refreshTest() throws Exception {
                LoadingCacheTest.loadingCacheTest(cacheWithRefresh1, 0);
                RefreshCacheTest.refreshCacheTest(cacheWithRefresh2, 200, 100);
                RefreshCacheTest.computeIfAbsentTest(cacheWithRefresh2);

                cacheWithRefresh3.config().setLoader((k) -> refreshCount++);
                cacheWithRefresh3.put("K1", "V1");
                Assert.assertEquals("V1", cacheWithRefresh3.get("K1"));
                Thread.sleep((long) (cacheWithRefresh3.config().getRefreshPolicy().getRefreshMillis() * 1.5));
                Assert.assertEquals(0, cacheWithRefresh3.get("K1"));

                cacheWithRefresh1.close();
                cacheWithRefresh2.close();
                cacheWithRefresh3.close();
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

            private void testCacheWithLocalExpire() {
                testCache1Config();
                testCache2Config();
                testCache3Config();
            }

            private void testCache1Config() {
                MultiLevelCacheConfig<?, ?> config = (MultiLevelCacheConfig<?, ?>) cacheWithLocalExpire_1.config();
                Assert.assertTrue(config.isUseExpireOfSubCache());
                Assert.assertEquals(2000, config.getExpireAfterWriteInMillis());
                Assert.assertEquals(1000, config.getCaches().get(0).config().getExpireAfterWriteInMillis());
                Assert.assertEquals(2000, config.getCaches().get(1).config().getExpireAfterWriteInMillis());
            }

            private void testCache2Config() {
                MultiLevelCacheConfig<?, ?> config = (MultiLevelCacheConfig<?, ?>) cacheWithLocalExpire_2.config();
                Assert.assertFalse(config.isUseExpireOfSubCache());
                Assert.assertEquals(2000, config.getExpireAfterWriteInMillis());
                Assert.assertEquals(2000, config.getCaches().get(0).config().getExpireAfterWriteInMillis());
                Assert.assertEquals(2000, config.getCaches().get(1).config().getExpireAfterWriteInMillis());
            }

            private void testCache3Config() {
                Assert.assertEquals(2000, cacheWithLocalExpire_3.config().getExpireAfterWriteInMillis());
            }

        }
    }

}
