package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import com.alicp.jetcache.template.QuickConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExternalCacheAutoInitTest {

    private TestExternalCacheAutoInit autoInit;
    private AutoConfigureBeans autoConfigureBeans;

    @BeforeEach
    public void setup() {
        autoInit = new TestExternalCacheAutoInit();
        autoConfigureBeans = new AutoConfigureBeans();
        autoInit.autoConfigureBeans = autoConfigureBeans;
    }

    @Test
    public void testParseWriteInterceptorFromSpringBean() {
        ExternalCacheWriteInterceptor interceptor = noopInterceptor();
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("loggingInterceptor")).thenReturn(interceptor);
        autoInit.applicationContext = applicationContext;

        ExternalCacheBuilder<?> builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        autoInit.parse(builder, config("externalWriteInterceptors", "bean:loggingInterceptor"));

        assertEquals(1, builder.getConfig().getWriteInterceptors().size());
        assertSame(interceptor, builder.getConfig().getWriteInterceptors().get(0));
    }

    @Test
    public void testParseWriteInterceptorFromCustomContainer() {
        ExternalCacheWriteInterceptor interceptor = noopInterceptor();
        autoConfigureBeans.getCustomContainer().put("logging", interceptor);

        ExternalCacheBuilder<?> builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        autoInit.parse(builder, config("externalWriteInterceptors", "logging"));

        assertEquals(1, builder.getConfig().getWriteInterceptors().size());
        assertSame(interceptor, builder.getConfig().getWriteInterceptors().get(0));
    }

    @Test
    public void testMissingBeanShouldThrowCacheConfigException() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("missingInterceptor"))
                .thenThrow(new NoSuchBeanDefinitionException("missingInterceptor"));
        autoInit.applicationContext = applicationContext;

        ExternalCacheBuilder<?> builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        assertThrows(CacheConfigException.class,
                () -> autoInit.parse(builder, config("externalWriteInterceptors", "bean:missingInterceptor")));
    }

    @Test
    public void testWrongBeanTypeShouldThrowCacheConfigException() {
        autoConfigureBeans.getCustomContainer().put("bad", new Object());
        ExternalCacheBuilder<?> builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        assertThrows(CacheConfigException.class,
                () -> autoInit.parse(builder, config("externalWriteInterceptors", "bad")));
    }

    @Test
    public void testStarterConfigShouldTriggerInterceptorAfterCacheBuild() {
        CountingInterceptor interceptor = new CountingInterceptor();
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("loggingInterceptor")).thenReturn(interceptor);
        autoInit.applicationContext = applicationContext;

        MockRemoteCacheBuilder<?> builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder();
        autoInit.parse(builder, config(
                "externalWriteInterceptors", "bean:loggingInterceptor",
                "keyPrefix", "testPrefix"));

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setLocalCacheBuilders(Collections.emptyMap());
        globalCacheConfig.setRemoteCacheBuilders(Collections.singletonMap(CacheConsts.DEFAULT_AREA, builder));

        SpringConfigProvider configProvider = new SpringConfigProvider();
        configProvider.setApplicationContext(applicationContext);
        configProvider.setGlobalCacheConfig(globalCacheConfig);
        configProvider.init();

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCacheBuilderTemplate(configProvider.getCacheBuilderTemplate());
        try {
            Cache<Object, Object> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder("starterWriteInterceptor")
                    .cacheType(CacheType.REMOTE)
                    .expire(Duration.ofSeconds(30))
                    .build());

            assertTrue(cache.PUT("K1", "V1").isSuccess());
            assertEquals(1, interceptor.getCount());
        } finally {
            cacheManager.close();
            configProvider.shutdown();
        }
    }

    private static ExternalCacheWriteInterceptor noopInterceptor() {
        return new ExternalCacheWriteInterceptor() {
            @Override
            public <K, V> void intercept(WriteContext<K, V> ctx) {
            }
        };
    }

    private static ConfigTree config(String key, String value) {
        return config(new String[]{key, value});
    }

    private static ConfigTree config(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("keyValues length must be even");
        }
        MockEnvironment environment = new MockEnvironment();
        for (int i = 0; i < keyValues.length; i += 2) {
            environment.withProperty(keyValues[i], keyValues[i + 1]);
        }
        return new ConfigTree(environment, "");
    }

    private static class CountingInterceptor implements ExternalCacheWriteInterceptor {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public <K, V> void intercept(WriteContext<K, V> ctx) {
            count.incrementAndGet();
        }

        int getCount() {
            return count.get();
        }
    }

    private static class TestExternalCacheAutoInit extends ExternalCacheAutoInit {
        TestExternalCacheAutoInit() {
            super("mock");
        }

        void parse(ExternalCacheBuilder<?> builder, ConfigTree ct) {
            parseGeneralConfig(builder, ct);
        }

        @Override
        protected CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix) {
            return null;
        }
    }
}
