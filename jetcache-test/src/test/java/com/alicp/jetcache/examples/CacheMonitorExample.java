package com.alicp.jetcache.examples;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MonitoredCache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorStatLogger;
import com.alicp.jetcache.support.FastjsonKeyConvertor;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/2.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheMonitorExample {
    public static void main(String[] args) throws Exception {
        int logDelayMillis = 500;
        DefaultCacheMonitorStatLogger statLogger = new DefaultCacheMonitorStatLogger(logDelayMillis);
        Cache<String, Integer> l1Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        Cache<String, Integer> l2Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        l1Cache = new MonitoredCache(l1Cache, new DefaultCacheMonitor("OrderCache_L1", 2, TimeUnit.SECONDS, statLogger));
        l2Cache = new MonitoredCache(l2Cache, new DefaultCacheMonitor("OrderCache_L2", 2, TimeUnit.SECONDS, statLogger));
        Cache<String, Integer> multiLevelCache = new MultiLevelCache<>(l1Cache, l2Cache);
        Cache<String, Integer> orderCache = new MonitoredCache<>(multiLevelCache, new DefaultCacheMonitor("OrderCache", 2, TimeUnit.SECONDS, statLogger));

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
    }
}
