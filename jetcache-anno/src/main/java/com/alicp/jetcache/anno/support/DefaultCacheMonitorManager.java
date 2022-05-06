/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheUtil;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultMetricsManager;
import com.alicp.jetcache.support.LocalCacheUpdater;
import com.alicp.jetcache.support.StatInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultCacheMonitorManager extends AbstractLifecycle implements CacheMonitorManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCacheMonitorManager.class);

    private DefaultMetricsManager defaultMetricsManager;

    @Resource
    private GlobalCacheConfig globalCacheConfig;

    @Autowired(required = false)
    private Consumer<StatInfo> metricsCallback;

    @Autowired(required = false)
    private BroadcastManager broadcastManager;

    @Resource
    private ConfigProvider configProvider;

    private final ConcurrentHashMap<String, BroadcastManager> broadcastManagers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalCacheUpdater> updaters = new ConcurrentHashMap<>();

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
        BroadcastManager bm = broadcastManager; // first use inject BroadcastManager
        if (bm == null) {
            if (!cacheBuilder.supportBroadcast()) {
                return;
            }
            bm = broadcastManagers.computeIfAbsent(area, keyNotUse -> {
                ExternalCacheBuilder builderCopy = (ExternalCacheBuilder) cacheBuilder.clone();
                return builderCopy.broadcastManager(builderCopy.getBroadcastChannel());
            });
        }
        if (bm == null) {
            return;
        }
        addNotifyMonitor(area, cacheName, cache, bm);
    }

    private void addNotifyMonitor(String area, String cacheName, Cache cache, BroadcastManager bm) {
        LocalCacheUpdater updater = updaters.computeIfAbsent(area, noUseParam -> {
            LocalCacheUpdater result = new LocalCacheUpdater(bm, configProvider.getCacheManager());
            bm.startSubscribe(result);
            return result;
        });
        if (updater != null) {
            updater.addNotifyMonitor(area, cacheName, cache);
        }
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
            if (m instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) m).close();
                } catch (Exception e) {
                    logger.warn("BroadcastManager close fail", e);
                }
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

