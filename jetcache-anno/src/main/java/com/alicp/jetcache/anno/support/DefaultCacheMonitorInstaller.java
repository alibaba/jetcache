/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import com.alicp.jetcache.support.StatInfo;
import com.alicp.jetcache.support.StatInfoLogger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultCacheMonitorInstaller extends AbstractLifecycle implements CacheMonitorInstaller {

    private DefaultCacheMonitorManager defaultCacheMonitorManager;

    @Resource
    private GlobalCacheConfig globalCacheConfig;

    @Autowired(required = false)
    private Consumer<StatInfo> statCallback = new StatInfoLogger(false);

    @Override
    public void addMonitors(String cacheName, Cache cache) {
        if (defaultCacheMonitorManager != null) {
            if (cache instanceof MultiLevelCache) {
                MultiLevelCache mc = (MultiLevelCache) cache;
                if (mc.caches().length == 2) {
                    Cache local = mc.caches()[0];
                    Cache remote = mc.caches()[1];
                    DefaultCacheMonitor localMonitor = new DefaultCacheMonitor(cacheName + "_local");
                    local.config().getMonitors().add(localMonitor);
                    DefaultCacheMonitor remoteMonitor = new DefaultCacheMonitor(cacheName + "_remote");
                    remote.config().getMonitors().add(remoteMonitor);
                    defaultCacheMonitorManager.add(localMonitor, remoteMonitor);
                }
            }

            DefaultCacheMonitor monitor = new DefaultCacheMonitor(cacheName);
            cache.config().getMonitors().add(monitor);
            defaultCacheMonitorManager.add(monitor);
        }
    }

    @Override
    protected void doInit() {
        if (globalCacheConfig.getStatIntervalMinutes() > 0) {
            defaultCacheMonitorManager = new DefaultCacheMonitorManager(globalCacheConfig.getStatIntervalMinutes(),
                    TimeUnit.MINUTES, statCallback);
            defaultCacheMonitorManager.start();
        }
    }

    @Override
    protected void doShutdown() {
        if (defaultCacheMonitorManager != null) {
            defaultCacheMonitorManager.stop();
        }
        defaultCacheMonitorManager = null;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }

    public void setStatCallback(Consumer<StatInfo> statCallback) {
        this.statCallback = statCallback;
    }
}

