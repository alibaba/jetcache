/**
 * Created on  13-09-04 15:34
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.MonitoredCache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.method.CacheInvokeContext;
import com.alicp.jetcache.anno.method.ClassUtil;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheContext {

    private static ThreadLocal<CacheThreadLocal> cacheThreadLocal = new ThreadLocal<CacheThreadLocal>() {
        @Override
        protected CacheThreadLocal initialValue() {
            return new CacheThreadLocal();
        }
    };

    private ConfigProvider configProvider = new ConfigProvider();
    private GlobalCacheConfig globalCacheConfig;

    private DefaultCacheMonitorManager defaultCacheMonitorManager;
    private CacheManager cacheManager;

    public CacheContext(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }

    protected void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @PostConstruct
    public synchronized void init() {
        if (cacheManager == null) {
            this.cacheManager = new CacheManager();
            if (globalCacheConfig.getStatIntervalMinutes() > 0) {
                defaultCacheMonitorManager = new DefaultCacheMonitorManager(globalCacheConfig.getStatIntervalMinutes(),
                        TimeUnit.MINUTES, globalCacheConfig.getConfigProvider().statCallback());
                defaultCacheMonitorManager.start();
            }
        }
    }

    @PreDestroy
    public synchronized void shutdown() {
        if (defaultCacheMonitorManager != null) {
            defaultCacheMonitorManager.stop();
        }
        cacheManager = null;
        defaultCacheMonitorManager = null;
    }

    public CacheInvokeContext createCacheInvokeContext() {
        CacheInvokeContext c = newCacheInvokeContext();
        c.setCacheFunction((invokeContext) -> {
            CacheAnnoConfig cacheAnnoConfig = invokeContext.getCacheInvokeConfig().getCacheAnnoConfig();
            String area = cacheAnnoConfig.getArea();
            String cacheName = cacheAnnoConfig.getName();
            if (CacheConsts.DEFAULT_NAME.equalsIgnoreCase(cacheName)) {
                cacheName = ClassUtil.generateCacheName(cacheAnnoConfig.getVersion(),
                        invokeContext.getMethod(), invokeContext.getHiddenPackages());
            }
            String fullCacheName = area + "_" + cacheName;
            Cache cache = cacheManager.getCache(fullCacheName);
            if (cache == null) {
                if (cacheAnnoConfig.getCacheType() == CacheType.LOCAL) {
                    cache = buildLocal(cacheAnnoConfig, area);
                } else if (cacheAnnoConfig.getCacheType() == CacheType.REMOTE) {
                    cache = buildRemote(cacheAnnoConfig, area, fullCacheName);
                } else {
                    Cache local = buildLocal(cacheAnnoConfig, area);
                    Cache remote = buildRemote(cacheAnnoConfig, area, fullCacheName);

                    if (defaultCacheMonitorManager != null) {
                        DefaultCacheMonitor localMonitor = new DefaultCacheMonitor(fullCacheName + "_local");
                        local = new MonitoredCache(local, localMonitor);
                        DefaultCacheMonitor remoteMonitor = new DefaultCacheMonitor(fullCacheName + "_remote");
                        remote = new MonitoredCache(remote, remoteMonitor);
                        defaultCacheMonitorManager.add(localMonitor, remoteMonitor);
                    }

                    cache = new MultiLevelCache(local, remote);
                }

                if (defaultCacheMonitorManager != null) {
                    DefaultCacheMonitor monitor = new DefaultCacheMonitor(fullCacheName);
                    cache = new MonitoredCache(cache, new DefaultCacheMonitor(fullCacheName));
                    defaultCacheMonitorManager.add(monitor);
                }

                cacheManager.addCache(fullCacheName, cache);
            }
            return cache;
        });

        return c;
    }

    private Cache buildRemote(CacheAnnoConfig cacheAnnoConfig, String area, String prefix) {
        ExternalCacheBuilder cacheBuilder = (ExternalCacheBuilder) globalCacheConfig.getRemoteCacheBuilders().get(area);
        if (cacheBuilder == null) {
            throw new CacheConfigException("no remote cache builder: " + area);
        }
        cacheBuilder = (ExternalCacheBuilder) cacheBuilder.clone();
        if (cacheBuilder == null) {
            throw new CacheConfigException("no CacheFactory with name \"" + area + "\" defined in remoteCacheFacotories");
        }
        cacheBuilder.setDefaultExpireInMillis(cacheAnnoConfig.getExpire() * 1000);
        cacheBuilder.setKeyPrefix(prefix);
        cacheBuilder.setValueEncoder(configProvider.parseValueEncoder(cacheAnnoConfig.getSerialPolicy()));
        cacheBuilder.setValueDecoder(configProvider.parseValueDecoder(cacheAnnoConfig.getSerialPolicy()));
        return cacheBuilder.buildCache();
    }

    private Cache buildLocal(CacheAnnoConfig cacheAnnoConfig, String area) {
        Cache cache;
        EmbeddedCacheBuilder cacheBuilder = (EmbeddedCacheBuilder) globalCacheConfig.getLocalCacheBuilders().get(area);
        if (cacheBuilder == null) {
            throw new CacheConfigException("no local cache builder: " + area);
        }
        cacheBuilder = (EmbeddedCacheBuilder) cacheBuilder.clone();
        if (cacheBuilder == null) {
            throw new CacheConfigException("no CacheFactory with name \"" + area + "\" defined in localCacheFactory");
        }
        cacheBuilder.setLimit(cacheAnnoConfig.getLocalLimit());
        cacheBuilder.setDefaultExpireInMillis(cacheAnnoConfig.getExpire() * 1000);
        cache = cacheBuilder.buildCache();
        return cache;
    }

    protected CacheInvokeContext newCacheInvokeContext() {
        return new CacheInvokeContext();
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Enable cache in current thread, for @Cached(enabled=false).
     *
     * @param callback
     * @see EnableCache
     */
    public static <T> T enableCache(Supplier<T> callback) {
        CacheThreadLocal var = cacheThreadLocal.get();
        try {
            var.setEnabledCount(var.getEnabledCount() + 1);
            return callback.get();
        } finally {
            var.setEnabledCount(var.getEnabledCount() - 1);
        }
    }

    protected static void enable() {
        CacheThreadLocal var = cacheThreadLocal.get();
        var.setEnabledCount(var.getEnabledCount() + 1);
    }

    protected static void disable() {
        CacheThreadLocal var = cacheThreadLocal.get();
        var.setEnabledCount(var.getEnabledCount() - 1);
    }

    protected static boolean isEnabled() {
        return cacheThreadLocal.get().getEnabledCount() > 0;
    }

}
