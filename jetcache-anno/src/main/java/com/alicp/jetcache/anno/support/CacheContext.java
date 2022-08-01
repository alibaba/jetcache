/**
 * Created on  13-09-04 15:34
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.method.CacheInvokeContext;
import com.alicp.jetcache.template.QuickConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheContext {

    private static Logger logger = LoggerFactory.getLogger(CacheContext.class);

    private static ThreadLocal<CacheThreadLocal> cacheThreadLocal = new ThreadLocal<CacheThreadLocal>() {
        @Override
        protected CacheThreadLocal initialValue() {
            return new CacheThreadLocal();
        }
    };

    private ConfigProvider configProvider;
    private GlobalCacheConfig globalCacheConfig;

    protected CacheManager cacheManager;

    public CacheContext(ConfigProvider configProvider, GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
        this.configProvider = configProvider;
        cacheManager = configProvider.getCacheManager();
    }

    public CacheInvokeContext createCacheInvokeContext(ConfigMap configMap) {
        CacheInvokeContext c = newCacheInvokeContext();
        c.setCacheFunction((cic, cac) -> createOrGetCache(cic, cac, configMap));
        return c;
    }

    private Cache createOrGetCache(CacheInvokeContext invokeContext, CacheAnnoConfig cacheAnnoConfig, ConfigMap configMap) {
        Cache cache = cacheAnnoConfig.getCache();
        if (cache == null) {
            if (cacheAnnoConfig instanceof CachedAnnoConfig) {
                cache = createCacheByCachedConfig((CachedAnnoConfig) cacheAnnoConfig, invokeContext);
            } else if ((cacheAnnoConfig instanceof CacheInvalidateAnnoConfig) || (cacheAnnoConfig instanceof CacheUpdateAnnoConfig)) {
                CachedAnnoConfig cac = configMap.getByCacheName(cacheAnnoConfig.getArea(), cacheAnnoConfig.getName());
                if (cac == null) {
                    String message = "can't find @Cached definition with area=" + cacheAnnoConfig.getArea()
                            + " name=" + cacheAnnoConfig.getName() +
                            ", specified in " + cacheAnnoConfig.getDefineMethod();
                    CacheConfigException e = new CacheConfigException(message);
                    logger.error("Cache operation aborted because can't find @Cached definition", e);
                    return null;
                }
                cache = createCacheByCachedConfig(cac, invokeContext);
            }
            cacheAnnoConfig.setCache(cache);
        }
        return cache;
    }

    private Cache createCacheByCachedConfig(CachedAnnoConfig ac, CacheInvokeContext invokeContext) {
        String area = ac.getArea();
        String cacheName = ac.getName();
        if (CacheConsts.isUndefined(cacheName)) {

            cacheName = configProvider.createCacheNameGenerator(invokeContext.getHiddenPackages())
                    .generateCacheName(invokeContext.getMethod(), invokeContext.getTargetObject());
        }
        Cache cache = __createOrGetCache(ac, area, cacheName);
        return cache;
    }

    @Deprecated
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return getCache(CacheConsts.DEFAULT_AREA, cacheName);
    }

    @Deprecated
    public <K, V> Cache<K, V> getCache(String area, String cacheName) {
        Cache cache = cacheManager.getCache(area, cacheName);
        return cache;
    }

    public Cache __createOrGetCache(CachedAnnoConfig cac, String area, String cacheName) {
        QuickConfig c = new QuickConfig();
        c.setArea(area);
        c.setName(cacheName);
        TimeUnit timeUnit = cac.getTimeUnit();
        if (cac.getExpire() > 0) {
            c.setExpire(Duration.ofMillis(timeUnit.toMillis(cac.getExpire())));
        }
        if (cac.getLocalExpire() > 0) {
            c.setLocalExpire(Duration.ofMillis(timeUnit.toMillis(cac.getLocalExpire())));
        }
        if (cac.getLocalLimit() > 0) {
            c.setLocalLimit(cac.getLocalLimit());
        }
        c.setCacheType(cac.getCacheType());
        c.setSyncLocal(cac.isSyncLocal());
        if (!CacheConsts.isUndefined(cac.getKeyConvertor())) {
            c.setKeyConvertor(configProvider.parseKeyConvertor(cac.getKeyConvertor()));
        }
        if (!CacheConsts.isUndefined(cac.getSerialPolicy())) {
            c.setValueEncoder(configProvider.parseValueEncoder(cac.getSerialPolicy()));
            c.setValueDecoder(configProvider.parseValueDecoder(cac.getSerialPolicy()));
        }
        c.setCacheNullValue(cac.isCacheNullValue());
        c.setUseAreaInPrefix(globalCacheConfig.isAreaInCacheName());
        PenetrationProtectConfig ppc = cac.getPenetrationProtectConfig();
        if (ppc != null) {
            c.setPenetrationProtect(ppc.isPenetrationProtect());
            c.setPenetrationProtectTimeout(ppc.getPenetrationProtectTimeout());
        }
        c.setRefreshPolicy(cac.getRefreshPolicy());
        return cacheManager.getOrCreateCache(c);
    }

    protected CacheInvokeContext newCacheInvokeContext() {
        return new CacheInvokeContext();
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
