/**
 * Created on  13-09-04 15:34
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.anno.method.CacheInvokeContext;
import com.alicp.jetcache.anno.method.ClassUtil;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
    private CacheManager cacheManager;
    private GlobalCacheConfig globalCacheConfig;

    private int statIntervalMinutes;
    private Consumer<DefaultCacheMonitorManager.StatInfo> statCallback;
    private DefaultCacheMonitorManager defaultCacheMonitorManager;

    public CacheContext(GlobalCacheConfig globalCacheConfig, int statIntervalMinutes, Consumer<DefaultCacheMonitorManager.StatInfo> statCallback) {
        this.globalCacheConfig = globalCacheConfig;
        this.statIntervalMinutes = statIntervalMinutes;
        this.statCallback = statCallback;
    }

    @PostConstruct
    public void init() {
        this.cacheManager = new CacheManager();
        if (statIntervalMinutes > 0) {
            if (statCallback == null) {
                defaultCacheMonitorManager = new DefaultCacheMonitorManager(statIntervalMinutes, TimeUnit.MINUTES);
            } else {
                defaultCacheMonitorManager = new DefaultCacheMonitorManager(statIntervalMinutes, TimeUnit.MINUTES, statCallback);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        if (defaultCacheMonitorManager != null) {
            defaultCacheMonitorManager.shutdown();
        }
    }

    public CacheInvokeContext createCacheInvokeContext() {
        CacheInvokeContext c = newCacheInvokeContext();
        c.setCacheFunction((invokeContext) -> {
            CacheAnnoConfig cacheAnnoConfig = invokeContext.getCacheInvokeConfig().getCacheAnnoConfig();
            String area = cacheAnnoConfig.getArea();
            String prefix = ClassUtil.getSubArea(cacheAnnoConfig.getVersion(),
                    invokeContext.getMethod(), invokeContext.getHiddenPackages());
            String cacheName = area + "_" + prefix;
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                if (cacheAnnoConfig.getCacheType() == CacheType.LOCAL) {
                    cache = buildLocal(cacheAnnoConfig, area);
                } else if (cacheAnnoConfig.getCacheType() == CacheType.REMOTE) {
                    cache = buildRemote(cacheAnnoConfig, area, prefix);
                } else {
                    Cache local = buildLocal(cacheAnnoConfig, area);
                    Cache remote = buildRemote(cacheAnnoConfig, area, prefix);

                    if (defaultCacheMonitorManager != null) {
                        DefaultCacheMonitor localMonitor = new DefaultCacheMonitor(cacheName + "_local");
                        local = new MonitoredCache(local, localMonitor);
                        DefaultCacheMonitor remoteMonitor = new DefaultCacheMonitor(cacheName + "_remote");
                        remote = new MonitoredCache(remote, remoteMonitor);
                        defaultCacheMonitorManager.add(localMonitor, remoteMonitor);
                    }

                    cache = new MultiLevelCache(local, remote);
                }

                if (defaultCacheMonitorManager != null) {
                    DefaultCacheMonitor monitor = new DefaultCacheMonitor(cacheName);
                    cache = new MonitoredCache(cache, new DefaultCacheMonitor(cacheName));
                    defaultCacheMonitorManager.add(monitor);
                }

                cacheManager.addCache(cacheName, cache);
            }
            return cache;
        });

        return c;
    }

    private Cache buildRemote(CacheAnnoConfig cacheAnnoConfig, String area, String prefix) {
        ExternalCacheBuilder cacheFactory = (ExternalCacheBuilder) globalCacheConfig.getRemoteCacheBuilders().get(area);
        if (cacheFactory == null) {
            throw new CacheConfigException("no CacheFactory with name \"" + area + "\" defined in remoteCacheFacotories");
        }
        cacheFactory.setDefaultExpireInMillis(cacheAnnoConfig.getExpire() * 1000);
        cacheFactory.setKeyPrefix(prefix);
        if (SerialPolicy.KRYO.equals(cacheAnnoConfig.getSerialPolicy())) {
            cacheFactory.setValueEncoder(KryoValueEncoder.INSTANCE);
            cacheFactory.setValueDecoder(KryoValueDecoder.INSTANCE);
        } else if (SerialPolicy.JAVA.equals(cacheAnnoConfig.getSerialPolicy())) {
            cacheFactory.setValueEncoder(JavaValueEncoder.INSTANCE);
            cacheFactory.setValueDecoder(JavaValueDecoder.INSTANCE);
        } else if (SerialPolicy.FASTJSON.equals(cacheAnnoConfig.getSerialPolicy())) {
            //noinspection deprecation
            cacheFactory.setValueEncoder(FastjsonValueEncoder.INSTANCE);
            //noinspection deprecation
            cacheFactory.setValueDecoder(FastjsonValueDecoder.INSTANCE);
        } else {
            throw new CacheException(cacheAnnoConfig.getSerialPolicy());
        }
        return cacheFactory.buildCache();
    }

    private Cache buildLocal(CacheAnnoConfig cacheAnnoConfig, String area) {
        Cache cache;
        EmbeddedCacheBuilder cacheFactory = (EmbeddedCacheBuilder) globalCacheConfig.getLocalCacheBuilders().get(area);
        if (cacheFactory == null) {
            throw new CacheConfigException("no CacheFactory with name \"" + area + "\" defined in localCacheFactory");
        }
        cacheFactory.setLimit(cacheAnnoConfig.getLocalLimit());
        cacheFactory.setDefaultExpireInMillis(cacheAnnoConfig.getExpire() * 1000);
        cache = cacheFactory.buildCache();
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
