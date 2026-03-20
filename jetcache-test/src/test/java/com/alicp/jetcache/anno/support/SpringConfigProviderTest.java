package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpringConfigProviderTest {

    private SpringConfigProvider configProvider;
    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        StaticApplicationContext context = new StaticApplicationContext();
        beanFactory = context.getDefaultListableBeanFactory();
        configProvider = new SpringConfigProvider();
        configProvider.setApplicationContext(context);
    }

    @Test
    public void testParseWriteInterceptorsWithBeanPrefix() {
        ExternalCacheWriteInterceptor interceptor = noopInterceptor();
        beanFactory.registerSingleton("loggingInterceptor", interceptor);

        List<ExternalCacheWriteInterceptor> interceptors =
                configProvider.parseWriteInterceptors("bean:loggingInterceptor");

        assertEquals(1, interceptors.size());
        assertSame(interceptor, interceptors.get(0));
    }

    @Test
    public void testParseWriteInterceptorsWithoutBeanPrefixShouldThrow() {
        ExternalCacheWriteInterceptor interceptor = noopInterceptor();
        beanFactory.registerSingleton("loggingInterceptor", interceptor);

        assertThrows(CacheConfigException.class,
                () -> configProvider.parseWriteInterceptors("loggingInterceptor"));
    }

    private static ExternalCacheWriteInterceptor noopInterceptor() {
        return new ExternalCacheWriteInterceptor() {
            @Override
            public <K, V> void intercept(WriteContext<K, V> ctx) {
            }
        };
    }
}
