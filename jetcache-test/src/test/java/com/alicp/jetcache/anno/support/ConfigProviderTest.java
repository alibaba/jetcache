/**
 * Created on 2022/08/02.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheMonitor;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import com.alicp.jetcache.template.QuickConfig;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author huangli
 */
public class ConfigProviderTest {
    private SimpleCacheManager cacheManager;
    private ConfigProvider configProvider;

    @BeforeEach
    public void setup() {
        MockRemoteCacheBuilder.reset();
        GlobalCacheConfig globalCacheConfig = TestUtil.createGloableConfig();
        globalCacheConfig.setStatIntervalMinutes(1);
        configProvider = new ConfigProvider();
        configProvider.setMetricsCallback(i -> {
        });
        configProvider.setGlobalCacheConfig(globalCacheConfig);
        configProvider.init();
        cacheManager = new JetCacheBaseBeans().cacheManager(configProvider);
    }

    @AfterEach
    public void teardown() {
        configProvider.shutdown();
        MockRemoteCacheBuilder.reset();
    }

    @Test
    public void test() {
        Assertions.assertEquals(2, cacheManager.getCacheBuilderTemplate().getCacheMonitorInstallers().size());
        RefreshPolicy rp = new RefreshPolicy();
        rp.setRefreshMillis(100);
        QuickConfig qc = QuickConfig.newBuilder("a")
                .cacheType(CacheType.BOTH)
                .syncLocal(true)
                .refreshPolicy(rp).build();
        Cache c = cacheManager.getOrCreateCache(qc);
        Assertions.assertTrue(MockRemoteCacheBuilder.isSubscribeStart());
        List<CacheMonitor> monitors = c.config().getMonitors();
        Assertions.assertEquals(2, monitors.size());
        c.put("K1", "V1");
        Assertions.assertEquals("K1", MockRemoteCacheBuilder.getLastPublishMessage().getKeys()[0]);
    }
}
