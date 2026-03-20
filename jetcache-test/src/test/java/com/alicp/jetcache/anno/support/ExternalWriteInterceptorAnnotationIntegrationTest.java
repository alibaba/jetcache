package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
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
        return TestUtil.createGloableConfig();
    }

    @Bean
    public CountingInterceptor countingInterceptor() {
        return new CountingInterceptor();
    }

    @Bean
    public CountBean countBean() {
        return new CountBean();
    }

    @Test
    public void testCachedAndCreateCacheShouldTriggerInterceptor() {
        CountingInterceptor interceptor = context.getBean(CountingInterceptor.class);
        CountBean bean = context.getBean(CountBean.class);

        interceptor.reset();
        String value = bean.count("K1");
        Assert.assertEquals(value, bean.count("K1"));
        Assert.assertEquals(1, interceptor.getCount());

        interceptor.reset();
        bean.putViaCreateCache("K2", "V2");
        Assert.assertEquals(1, interceptor.getCount());
    }

    public static class CountBean {
        @CreateCache(name = "annoCreateCacheWriteInterceptor",
                cacheType = CacheType.REMOTE,
                expire = 3,
                timeUnit = TimeUnit.SECONDS,
                externalWriteInterceptors = "bean:countingInterceptor")
        private Cache<Object, Object> createCache;

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

        public void putViaCreateCache(String key, String value) {
            createCache.put(key, value);
        }
    }

    public static class CountingInterceptor implements ExternalCacheWriteInterceptor {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public <K, V> void intercept(WriteContext<K, V> ctx) {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }

        public void reset() {
            count.set(0);
        }
    }
}
