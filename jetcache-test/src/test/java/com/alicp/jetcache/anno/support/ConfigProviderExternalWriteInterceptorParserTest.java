package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigProviderExternalWriteInterceptorParserTest {

    private ConfigProvider configProvider;
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        GlobalCacheConfig globalCacheConfig = TestUtil.createGloableConfig();
        configProvider = new ConfigProvider();
        configProvider.setGlobalCacheConfig(globalCacheConfig);
        configProvider.init();
        cacheManager = new JetCacheBaseBeans().cacheManager(configProvider);
    }

    @Test
    public void testCreateCacheShouldThrowWhenExternalWriteInterceptorsConfiguredWithoutParser() {
        CachedAnnoConfig config = new CachedAnnoConfig();
        config.setArea(CacheConsts.DEFAULT_AREA);
        config.setName("writeInterceptorCache");
        config.setTimeUnit(TimeUnit.SECONDS);
        config.setExpire(60);
        config.setCacheType(CacheType.REMOTE);
        config.setCacheNullValue(CacheConsts.DEFAULT_CACHE_NULL_VALUE);
        config.setExternalWriteInterceptors("bean:loggingInterceptor");

        CacheContext cacheContext = configProvider.newContext(cacheManager);

        assertThrows(CacheConfigException.class,
                () -> cacheContext.__createOrGetCache(config, config.getArea(), config.getName()));
    }

    @Test
    public void testCreateCacheShouldThrowWhenExternalWriteInterceptorsConfiguredForLocalCache() {
        CachedAnnoConfig config = new CachedAnnoConfig();
        config.setArea(CacheConsts.DEFAULT_AREA);
        config.setName("localWriteInterceptorCache");
        config.setTimeUnit(TimeUnit.SECONDS);
        config.setExpire(60);
        config.setCacheType(CacheType.LOCAL);
        config.setCacheNullValue(CacheConsts.DEFAULT_CACHE_NULL_VALUE);
        config.setExternalWriteInterceptors("bean:loggingInterceptor");

        CacheContext cacheContext = configProvider.newContext(cacheManager);

        assertThrows(CacheConfigException.class,
                () -> cacheContext.__createOrGetCache(config, config.getArea(), config.getName()));
    }
}
