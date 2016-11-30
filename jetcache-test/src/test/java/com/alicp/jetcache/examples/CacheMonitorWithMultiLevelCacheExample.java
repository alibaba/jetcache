package com.alicp.jetcache.examples;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MonitoredCache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.CaffeineCache;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import com.alicp.jetcache.support.FastjsonKeyConvertor;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/2.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheMonitorWithMultiLevelCacheExample {
    public static void main(String[] args) throws Exception {
        Cache<String, Integer> l1Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new CaffeineCache((EmbeddedCacheConfig) c))
                .buildCache();
        Cache<String, Integer> l2Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new CaffeineCache((EmbeddedCacheConfig) c))
                .buildCache();
        DefaultCacheMonitor l1CacheMonitor = new DefaultCacheMonitor("OrderCache_L1");
        DefaultCacheMonitor l2CacheMonitor = new DefaultCacheMonitor("OrderCache_L2");
        DefaultCacheMonitor orderCacheMonitor = new DefaultCacheMonitor("OrderCache");

        l1Cache = new MonitoredCache(l1Cache, l1CacheMonitor);
        l2Cache = new MonitoredCache(l2Cache, l2CacheMonitor);
        Cache<String, Integer> multiLevelCache = new MultiLevelCache<>(l1Cache, l2Cache);
        Cache<String, Integer> orderCache = new MonitoredCache<>(multiLevelCache, orderCacheMonitor);

        boolean verboseLog = false;
        DefaultCacheMonitorManager statLogger = new DefaultCacheMonitorManager(1, TimeUnit.SECONDS, verboseLog);
        statLogger.add(l1CacheMonitor, l2CacheMonitor, orderCacheMonitor);
        statLogger.start();

        Thread t = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                orderCache.put("20161111", 123456789);
                orderCache.get("20161111");
                orderCache.get("20161212");
                orderCache.invalidate("20161111");
                orderCache.invalidate("20161212");
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
        });
        t.start();
        t.join();

        statLogger.stop();
    }
}
