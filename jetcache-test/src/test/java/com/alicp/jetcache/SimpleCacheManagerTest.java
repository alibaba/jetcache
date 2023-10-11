/**
 * Created on 2022/08/02.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.template.CacheBuilderTemplate;
import com.alicp.jetcache.template.QuickConfig;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author huangli
 */
public class SimpleCacheManagerTest {

    private SimpleCacheManager cacheManager;

    @BeforeEach
    public void setup() {
        cacheManager = new SimpleCacheManager();
        GlobalCacheConfig globalCacheConfig = TestUtil.createGloableConfig();
        CacheBuilderTemplate cb = new CacheBuilderTemplate(false,
                globalCacheConfig.getLocalCacheBuilders(), globalCacheConfig.getRemoteCacheBuilders());
        cacheManager.setCacheBuilderTemplate(cb);
    }

    @AfterEach
    public void teardown() {
        cacheManager.close();
    }

    @Test
    public void testDefault() {
        String cacheName = UUID.randomUUID().toString();
        cacheManager.getOrCreateCache(QuickConfig.newBuilder(cacheName).build());
        ExternalCacheBuilder cb = (ExternalCacheBuilder) cacheManager
                .getCacheBuilderTemplate().getCacheBuilder(1, CacheConsts.DEFAULT_AREA);
        Cache c = cacheManager.getCache(cacheName);
        assertTrue(c instanceof AbstractExternalCache);
        ExternalCacheConfig config = (ExternalCacheConfig) c.config();
        assertEquals(cb.getConfig().getExpireAfterWriteInMillis(), c.config().getExpireAfterWriteInMillis());
        assertSame(cb.getConfig().getKeyConvertor(), config.getKeyConvertor());
        assertSame(cb.getConfig().getValueEncoder(), config.getValueEncoder());
        assertSame(cb.getConfig().getValueDecoder(), config.getValueDecoder());
        assertEquals(cb.getConfig().isCacheNullValue(), config.isCacheNullValue());
        assertEquals(cb.getConfig().getKeyPrefix() + cacheName, config.getKeyPrefix());
        assertEquals(cb.getConfig().isCachePenetrationProtect(), config.isCachePenetrationProtect());
        assertEquals(cb.getConfig().getPenetrationProtectTimeout(), config.getPenetrationProtectTimeout());
        assertNull(c.config().getRefreshPolicy());
    }

    @Test
    public void testMultiLevelCache() {
        String cacheName = UUID.randomUUID().toString();
        RefreshPolicy rp = new RefreshPolicy();
        rp.setRefreshMillis(100);
        rp.setRefreshLockTimeoutMillis(200);
        rp.setStopRefreshAfterLastAccessMillis(300);

        Function keyConvertor = k -> k;
        Function valueEncoder = k -> k;
        Function valueDecoder = k -> k;
        QuickConfig qc = QuickConfig.newBuilder(cacheName)
                .refreshPolicy(rp)
                .keyConvertor(keyConvertor)
                .valueEncoder(valueEncoder)
                .valueDecoder(valueDecoder)
                .expire(Duration.ofSeconds(2))
                .localExpire(Duration.ofSeconds(1))
                .localLimit(1)
                .useAreaInPrefix(true)
                .cacheNullValue(true)
                .penetrationProtect(true)
                .penetrationProtectTimeout(Duration.ofSeconds(20))
                .cacheType(CacheType.BOTH)
                .build();
        cacheManager.getOrCreateCache(qc);
        ExternalCacheBuilder cb = (ExternalCacheBuilder) cacheManager
                .getCacheBuilderTemplate().getCacheBuilder(1, CacheConsts.DEFAULT_AREA);
        RefreshCache c = (RefreshCache) cacheManager.getCache(cacheName);
        MultiLevelCache mc = (MultiLevelCache) c.getTargetCache();
        MultiLevelCacheConfig multiConfig = mc.config();
        EmbeddedCacheConfig localConfig = (EmbeddedCacheConfig) mc.caches()[0].config();
        ExternalCacheConfig remoteConfig = (ExternalCacheConfig) mc.caches()[1].config();
        assertEquals(2000, remoteConfig.getExpireAfterWriteInMillis());
        assertEquals(1000, localConfig.getExpireAfterWriteInMillis());
        assertEquals(1, localConfig.getLimit());
        assertSame(keyConvertor, localConfig.getKeyConvertor());
        assertSame(keyConvertor, remoteConfig.getKeyConvertor());
        assertSame(valueEncoder, remoteConfig.getValueEncoder());
        assertSame(valueDecoder, remoteConfig.getValueDecoder());
        assertTrue(localConfig.isCacheNullValue());
        assertTrue(remoteConfig.isCacheNullValue());
        assertEquals(cb.getConfig().getKeyPrefix() + CacheConsts.DEFAULT_AREA + "_" + cacheName,
                remoteConfig.getKeyPrefix());
        assertTrue(multiConfig.isCachePenetrationProtect());
        assertEquals(Duration.ofSeconds(20), multiConfig.getPenetrationProtectTimeout());
        assertSame(rp, multiConfig.getRefreshPolicy());
    }

    @Test
    public void testLocal() {
        String cacheName = UUID.randomUUID().toString();
        cacheManager.getOrCreateCache(QuickConfig.newBuilder(cacheName).cacheType(CacheType.LOCAL).build());
        Cache c = cacheManager.getCache(cacheName);
        assertTrue(c instanceof AbstractEmbeddedCache);
    }

    @Test
    public void testLoader() {
        String cacheName = UUID.randomUUID().toString();
        Cache<String, String> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder(cacheName).loader(k -> k + "V").build());
        assertEquals("K1V", cache.get("K1"));
    }

    @Test
    public void testRefresh() {
        String cacheName = UUID.randomUUID().toString();
        AtomicInteger count = new AtomicInteger();
        Cache<String, String> cache = cacheManager.getOrCreateCache(QuickConfig.newBuilder(cacheName)
                .loader(k -> String.valueOf(k) + count.getAndIncrement())
                .refreshPolicy(RefreshPolicy.newPolicy(10, TimeUnit.MILLISECONDS))
                .build());
        assertEquals("K10", cache.get("K1"));
        TestUtil.waitUtil(() -> !"K10".equals(cache.get("K1")));
    }
}
