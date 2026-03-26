package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExternalWriteInterceptorAnnotationIntegrationTest.class)
@Configuration
@EnableMethodCache(basePackages = {
        "com.alicp.jetcache.anno.support.ExternalWriteInterceptorAnnotationIntegrationTest"
})
@EnableCreateCacheAnnotation
@Import(JetCacheBaseBeans.class)
public class ExternalWriteInterceptorAnnotationIntegrationTest extends SpringTestBase {

    @Bean
    public GlobalCacheConfig config() {
        GlobalCacheConfig config = TestUtil.createGloableConfig();
        MockRemoteCacheBuilder remoteBuilder = (MockRemoteCacheBuilder)
                config.getRemoteCacheBuilders().get(CacheConsts.DEFAULT_AREA);
        remoteBuilder.addWriteInterceptor(globalCountingInterceptor());
        return config;
    }

    @Bean
    public CountingInterceptor countingInterceptor() {
        return new CountingInterceptor();
    }

    @Bean
    public CountingInterceptor globalCountingInterceptor() {
        return new CountingInterceptor();
    }

    @Bean
    public CountBean countBean() {
        return new CountBean();
    }

    @Test
    public void testCachedAndCreateCacheShouldTriggerInterceptor() {
        CountingInterceptor interceptor = (CountingInterceptor) context.getBean("countingInterceptor");
        CountingInterceptor globalInterceptor = (CountingInterceptor) context.getBean("globalCountingInterceptor");
        CountBean bean = context.getBean(CountBean.class);

        interceptor.reset();
        globalInterceptor.reset();
        String value = bean.count("K1");
        Assert.assertEquals(value, bean.count("K1"));
        Assert.assertEquals(1, interceptor.getCount());
        Assert.assertEquals(0, globalInterceptor.getCount());

        interceptor.reset();
        globalInterceptor.reset();
        bean.putViaCreateCache("K2", "V2");
        Assert.assertEquals(1, interceptor.getCount());
        Assert.assertEquals(0, globalInterceptor.getCount());
        Assert.assertEquals("V2", bean.getViaCreateCache("K2"));
    }

    @Test
    public void testEmptyAnnotationConfigShouldDisableGlobalInterceptor() {
        CountingInterceptor interceptor = (CountingInterceptor) context.getBean("countingInterceptor");
        CountingInterceptor globalInterceptor = (CountingInterceptor) context.getBean("globalCountingInterceptor");
        CountBean bean = context.getBean(CountBean.class);

        interceptor.reset();
        globalInterceptor.reset();
        String value = bean.countWithoutInterceptor("K3");
        Assert.assertEquals(value, bean.countWithoutInterceptor("K3"));
        Assert.assertEquals(0, interceptor.getCount());
        Assert.assertEquals(0, globalInterceptor.getCount());

        interceptor.reset();
        globalInterceptor.reset();
        bean.putViaCreateCacheWithoutInterceptor("K4", "V4");
        Assert.assertEquals(0, interceptor.getCount());
        Assert.assertEquals(0, globalInterceptor.getCount());
        Assert.assertEquals("V4", bean.getViaCreateCacheWithoutInterceptor("K4"));
    }

    public static class CountBean {
        @CreateCache(name = "annoCreateCacheWriteInterceptor",
                cacheType = CacheType.REMOTE,
                expire = 3,
                timeUnit = TimeUnit.SECONDS,
                externalWriteInterceptors = "bean:countingInterceptor")
        private Cache<Object, Object> createCache;

        @CreateCache(name = "annoCreateCacheWriteInterceptorDisabled",
                cacheType = CacheType.REMOTE,
                expire = 3,
                timeUnit = TimeUnit.SECONDS,
                externalWriteInterceptors = "")
        private Cache<Object, Object> createCacheWithoutInterceptor;

        private int i;

        @Cached(name = "annoCachedWriteInterceptor",
                cacheType = CacheType.REMOTE,
                expire = 3,
                timeUnit = TimeUnit.SECONDS,
                key = "#key",
                externalWriteInterceptors = "bean:countingInterceptor")
        public String count(String key) {
            return key + i++;
        }

        @Cached(name = "annoCachedWriteInterceptorDisabled",
                cacheType = CacheType.REMOTE,
                expire = 3,
                timeUnit = TimeUnit.SECONDS,
                key = "#key",
                externalWriteInterceptors = "")
        public String countWithoutInterceptor(String key) {
            return key + i++;
        }

        public void putViaCreateCache(String key, String value) {
            createCache.put(key, value);
        }

        public Object getViaCreateCache(String key) {
            return createCache.get(key);
        }

        public void putViaCreateCacheWithoutInterceptor(String key, String value) {
            createCacheWithoutInterceptor.put(key, value);
        }

        public Object getViaCreateCacheWithoutInterceptor(String key) {
            return createCacheWithoutInterceptor.get(key);
        }
    }

    public static class CountingInterceptor implements ExternalCacheWriteInterceptor {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public <K, V> WriteInterceptDecision intercept(WriteContext<K, V> ctx) {
            count.incrementAndGet();
            return WriteInterceptDecision.allow();
        }

        public int getCount() {
            return count.get();
        }

        public void reset() {
            count.set(0);
        }
    }
}
