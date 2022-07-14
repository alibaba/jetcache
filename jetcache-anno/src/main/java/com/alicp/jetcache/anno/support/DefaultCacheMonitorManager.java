/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheMonitor;
import com.alicp.jetcache.CacheUtil;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessageConsumer;
import com.alicp.jetcache.support.CacheNotifyMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultMetricsManager;
import com.alicp.jetcache.support.StatInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultCacheMonitorManager extends AbstractLifecycle implements CacheMonitorManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheMonitorManager.class);

    private final String sourceId = UUID.randomUUID().toString();

    private DefaultMetricsManager defaultMetricsManager;

    @Resource
    private GlobalCacheConfig globalCacheConfig;

    @Autowired(required = false)
    private Consumer<StatInfo> metricsCallback;

    @Autowired(required = false)
    private BroadcastManager broadcastManager;

    private boolean broadcastManagerInit;

    @Resource
    private ConfigProvider configProvider;

    private final ConcurrentHashMap<String, BroadcastManager> broadcastManagers = new ConcurrentHashMap<>();

    @Override
    public void addMonitors(String area, String cacheName, Cache cache, boolean syncLocal) {
        addMetricsMonitor(area, cacheName, cache);
        addNotifyMonitor(area, cacheName, cache, syncLocal);
    }

    protected void addNotifyMonitor(String area, String cacheName, Cache cache, boolean syncLocal) {
        if (!syncLocal) {
            return;
        }
        if (!(CacheUtil.getAbstractCache(cache) instanceof MultiLevelCache)) {
            return;
        }
        final ExternalCacheBuilder cacheBuilder = (ExternalCacheBuilder) globalCacheConfig.getRemoteCacheBuilders().get(area);
        if (cacheBuilder == null) {
            return;
        }

        BroadcastManager bm = broadcastManagers.computeIfAbsent(area, keyNotUse -> {
            if (broadcastManager != null) {
                if (!broadcastManagerInit) {
                    broadcastManager.startSubscribe(new CacheMessageConsumer(sourceId, configProvider.getCacheManager()));
                    broadcastManagerInit = true;
                }
                return broadcastManager; // first use inject BroadcastManager
            }
            if (!cacheBuilder.supportBroadcast()) {
                return null;
            }
            ExternalCacheBuilder builderCopy = (ExternalCacheBuilder) cacheBuilder.clone();
            MultiLevelCache mc = (MultiLevelCache) CacheUtil.getAbstractCache(cache);
            ExternalCacheConfig cacheConfig = (ExternalCacheConfig) mc.caches()[mc.caches().length - 1].config();
            builderCopy.setValueEncoder(cacheConfig.getValueEncoder());
            builderCopy.setValueDecoder(cacheConfig.getValueDecoder());
            BroadcastManager result = builderCopy.createBroadcastManager();
            result.startSubscribe(new CacheMessageConsumer(sourceId, configProvider.getCacheManager()));
            return result;
        });
        if (bm == null) {
            return;
        }
        CacheMonitor monitor = new CacheNotifyMonitor(bm, area, cacheName, cache, sourceId);
        cache.config().getMonitors().add(monitor);
    }

    protected void addMetricsMonitor(String area, String cacheName, Cache cache) {
        if (defaultMetricsManager != null) {
            cache = CacheUtil.getAbstractCache(cache);
            if (cache instanceof MultiLevelCache) {
                MultiLevelCache mc = (MultiLevelCache) cache;
                if (mc.caches().length == 2) {
                    Cache local = mc.caches()[0];
                    Cache remote = mc.caches()[1];
                    DefaultCacheMonitor localMonitor = new DefaultCacheMonitor(cacheName + "_local");
                    local.config().getMonitors().add(localMonitor);
                    DefaultCacheMonitor remoteMonitor = new DefaultCacheMonitor(cacheName + "_remote");
                    remote.config().getMonitors().add(remoteMonitor);
                    defaultMetricsManager.add(localMonitor, remoteMonitor);
                }
            }

            DefaultCacheMonitor monitor = new DefaultCacheMonitor(cacheName);
            cache.config().getMonitors().add(monitor);
            defaultMetricsManager.add(monitor);
        }
    }

    @Override
    protected void doInit() {
        initMetricsMonitor();
    }

    protected void initMetricsMonitor() {
        if (globalCacheConfig.getStatIntervalMinutes() > 0) {
            defaultMetricsManager = new DefaultMetricsManager(globalCacheConfig.getStatIntervalMinutes(),
                    TimeUnit.MINUTES, metricsCallback);
            defaultMetricsManager.start();
        }
    }

    @Override
    protected void doShutdown() {
        shutdownMetricsMonitor();
        for (BroadcastManager m : broadcastManagers.values()) {
            try {
                m.close();
            } catch (Exception e) {
                logger.warn("BroadcastManager close fail", e);
            }
        }
    }

    protected void shutdownMetricsMonitor() {
        if (defaultMetricsManager != null) {
            defaultMetricsManager.stop();
        }
        defaultMetricsManager = null;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }

    public void setMetricsCallback(Consumer<StatInfo> metricsCallback) {
        this.metricsCallback = metricsCallback;
    }

    public void setBroadcastManager(BroadcastManager broadcastManager) {
        this.broadcastManager = broadcastManager;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }
}

