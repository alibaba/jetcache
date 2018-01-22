/**
 * Created on  13-09-04 15:34
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.MultiLevelCacheBuilder;
import com.alicp.jetcache.RefreshCache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.method.CacheInvokeConfig;
import com.alicp.jetcache.anno.method.CacheInvokeContext;
import com.alicp.jetcache.anno.method.ClassUtil;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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

    public CacheInvokeContext createCacheInvokeContext(ConfigMap configMap) {
        CacheInvokeContext c = newCacheInvokeContext();
        c.setCacheFunction((invokeContext) -> {
            CacheInvokeConfig cic = invokeContext.getCacheInvokeConfig();
            Cache cache = cic.getCache();
            if (cache == null) {
                if (cic.getCachedAnnoConfig() != null) {
                    cache = createCacheByCachedConfig(cic, invokeContext.getMethod(), invokeContext.getHiddenPackages());
                } else if (cic.getInvalidateAnnoConfig() != null) {
                    CacheInvalidateAnnoConfig invalidateConfig = cic.getInvalidateAnnoConfig();
                    CacheInvokeConfig cacheDefineConfig = configMap.getByCacheName(invalidateConfig.getArea(), invalidateConfig.getName());
                    cache = createCacheByCachedConfig(cacheDefineConfig, invokeContext.getMethod(), invokeContext.getHiddenPackages());
                }
            }
            return cache;
        });
        return c;
    }

    private Cache createCacheByCachedConfig(CacheInvokeConfig cic, Method method, String[] hiddenPackages) {
        CachedAnnoConfig ac = cic.getCachedAnnoConfig();
        String area = ac.getArea();
        String cacheName = ac.getName();
        if (CacheConsts.UNDEFINED_STRING.equalsIgnoreCase(cacheName)) {
            cacheName = ClassUtil.generateCacheName(method, hiddenPackages);
        }
        Cache cache = __createOrGetCache(ac, area, cacheName);
        cic.setCache(cache);
        return cache;
    }

    public Cache __createOrGetCache(CachedAnnoConfig cachedAnnoConfig, String area, String cacheName) {
        String fullCacheName = area + "_" + cacheName;
        Cache cache = cacheManager.getCache(fullCacheName);
        if (cache == null) {
            synchronized (this) {
                cache = cacheManager.getCache(fullCacheName);
                if (cache == null) {
                    cache = buildCache(cachedAnnoConfig, area, fullCacheName);
                    cacheManager.addCache(fullCacheName, cache);
                }
            }
        }
        return cache;
    }

    protected Cache buildCache(CachedAnnoConfig cachedAnnoConfig, String area, String fullCacheName) {
        Cache cache;
        if (cachedAnnoConfig.getCacheType() == CacheType.LOCAL) {
            cache = buildLocal(cachedAnnoConfig, area);
        } else if (cachedAnnoConfig.getCacheType() == CacheType.REMOTE) {
            cache = buildRemote(cachedAnnoConfig, area, fullCacheName);
        } else {
            Cache local = buildLocal(cachedAnnoConfig, area);
            Cache remote = buildRemote(cachedAnnoConfig, area, fullCacheName);

            if (defaultCacheMonitorManager != null) {
                DefaultCacheMonitor localMonitor = new DefaultCacheMonitor(fullCacheName + "_local");
                local.config().getMonitors().add(localMonitor);
                DefaultCacheMonitor remoteMonitor = new DefaultCacheMonitor(fullCacheName + "_remote");
                remote.config().getMonitors().add(remoteMonitor);
                defaultCacheMonitorManager.add(localMonitor, remoteMonitor);
            }

            cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                    .expireAfterWrite(local.config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS)
                    .addCache(local, remote)
                    .buildCache();
        }
        cache = new RefreshCache(cache);

        if (defaultCacheMonitorManager != null) {
            DefaultCacheMonitor monitor = new DefaultCacheMonitor(fullCacheName);
            cache.config().getMonitors().add(monitor);
            defaultCacheMonitorManager.add(monitor);
        }
        return cache;
    }

    protected Cache buildRemote(CachedAnnoConfig cachedAnnoConfig, String area, String cacheName) {
        ExternalCacheBuilder cacheBuilder = (ExternalCacheBuilder) globalCacheConfig.getRemoteCacheBuilders().get(area);
        if (cacheBuilder == null) {
            throw new CacheConfigException("no remote cache builder: " + area);
        }
        cacheBuilder = (ExternalCacheBuilder) cacheBuilder.clone();

        if (cachedAnnoConfig.getExpire() > 0 ) {
            cacheBuilder.expireAfterWrite(cachedAnnoConfig.getExpire(), cachedAnnoConfig.getTimeUnit());
        }
        if (cacheBuilder.getConfig().getKeyPrefix() != null) {
            cacheBuilder.setKeyPrefix(cacheBuilder.getConfig().getKeyPrefix() + cacheName);
        } else {
            cacheBuilder.setKeyPrefix(cacheName);
        }
        if (!CacheConsts.UNDEFINED_STRING.equals(cachedAnnoConfig.getKeyConvertor())) {
            cacheBuilder.setKeyConvertor(configProvider.parseKeyConvertor(cachedAnnoConfig.getKeyConvertor()));
        }
        if (!CacheConsts.UNDEFINED_STRING.equals(cachedAnnoConfig.getSerialPolicy())) {
            cacheBuilder.setValueEncoder(configProvider.parseValueEncoder(cachedAnnoConfig.getSerialPolicy()));
            cacheBuilder.setValueDecoder(configProvider.parseValueDecoder(cachedAnnoConfig.getSerialPolicy()));
        }
        return cacheBuilder.buildCache();
    }

    protected Cache buildLocal(CachedAnnoConfig cachedAnnoConfig, String area) {
        Cache cache;
        EmbeddedCacheBuilder cacheBuilder = (EmbeddedCacheBuilder) globalCacheConfig.getLocalCacheBuilders().get(area);
        if (cacheBuilder == null) {
            throw new CacheConfigException("no local cache builder: " + area);
        }
        cacheBuilder = (EmbeddedCacheBuilder) cacheBuilder.clone();

        if (cachedAnnoConfig.getLocalLimit() != CacheConsts.UNDEFINED_INT) {
            cacheBuilder.setLimit(cachedAnnoConfig.getLocalLimit());
        }
        if (cachedAnnoConfig.getExpire() > 0) {
            cacheBuilder.expireAfterWrite(cachedAnnoConfig.getExpire(), cachedAnnoConfig.getTimeUnit());
        }
        if (!CacheConsts.UNDEFINED_STRING.equals(cachedAnnoConfig.getKeyConvertor())) {
            cacheBuilder.setKeyConvertor(configProvider.parseKeyConvertor(cachedAnnoConfig.getKeyConvertor()));
        }
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
