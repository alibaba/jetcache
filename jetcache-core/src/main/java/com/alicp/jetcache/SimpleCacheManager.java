/**
 * Created on 2019/2/1.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.template.CacheBuilderTemplate;
import com.alicp.jetcache.template.CacheMonitorInstaller;
import com.alicp.jetcache.template.QuickConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author huangli
 */
public class SimpleCacheManager implements CacheManager, AutoCloseable {

    private static final boolean DEFAULT_CACHE_NULL_VALUE = false;

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheManager.class);

    // area -> cacheName -> Cache
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Cache>> caches = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, BroadcastManager> broadcastManagers = new ConcurrentHashMap();

    private CacheBuilderTemplate cacheBuilderTemplate;

    public SimpleCacheManager() {
    }

    @Override
    public void close() {
        broadcastManagers.forEach((area, bm) -> {
            try {
                bm.close();
            } catch (Exception e) {
                logger.error("error during close broadcast manager", e);
            }
        });
        broadcastManagers.clear();
        caches.forEach((area, areaMap) -> {
            areaMap.forEach((cacheName, cache) -> {
                try {
                    cache.close();
                } catch (Exception e) {
                    logger.error("error during close Cache", e);
                }
            });
        });
        caches.clear();
    }

    private ConcurrentHashMap<String, Cache> getCachesByArea(String area) {
        return caches.computeIfAbsent(area, (key) -> new ConcurrentHashMap<>());
    }

    @Override
    public Cache getCache(String area, String cacheName) {
        ConcurrentHashMap<String, Cache> areaMap = getCachesByArea(area);
        return areaMap.get(cacheName);
    }

    @Override
    public void putCache(String area, String cacheName, Cache cache) {
        ConcurrentHashMap<String, Cache> areaMap = getCachesByArea(area);
        areaMap.put(cacheName, cache);
    }

    @Override
    public BroadcastManager getBroadcastManager(String area) {
        return broadcastManagers.get(area);
    }

    @Override
    public void putBroadcastManager(String area, BroadcastManager broadcastManager) {
        broadcastManagers.put(area, broadcastManager);
    }

    public CacheBuilderTemplate getCacheBuilderTemplate() {
        return cacheBuilderTemplate;
    }

    public void setCacheBuilderTemplate(CacheBuilderTemplate cacheBuilderTemplate) {
        this.cacheBuilderTemplate = cacheBuilderTemplate;
    }

    @Override
    public <K, V> Cache<K, V> getOrCreateCache(QuickConfig config) {
        if (cacheBuilderTemplate == null) {
            throw new IllegalStateException("cacheBuilderTemplate not set");
        }
        Objects.requireNonNull(config.getArea());
        Objects.requireNonNull(config.getName());
        ConcurrentHashMap<String, Cache> m = getCachesByArea(config.getArea());
        Cache c = m.get(config.getName());
        if (c != null) {
            return c;
        }
        return m.computeIfAbsent(config.getName(), n -> create(config));
    }

    private Cache create(QuickConfig config) {
        Cache cache;
        if (config.getCacheType() == null || config.getCacheType() == CacheType.REMOTE) {
            cache = buildRemote(config);
        } else if (config.getCacheType() == CacheType.LOCAL) {
            cache = buildLocal(config);
        } else {
            Cache local = buildLocal(config);
            Cache remote = buildRemote(config);


            boolean useExpireOfSubCache = config.getLocalExpire() != null;
            cache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                    .expireAfterWrite(remote.config().getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS)
                    .addCache(local, remote)
                    .useExpireOfSubCache(useExpireOfSubCache)
                    .cacheNullValue(config.getCacheNullValue() != null ?
                            config.getCacheNullValue() : DEFAULT_CACHE_NULL_VALUE)
                    .buildCache();
        }
        if (config.getRefreshPolicy() != null) {
            cache = new RefreshCache(cache);
        } else if (config.getLoader() != null) {
            cache = new LoadingCache(cache);
        }
        cache.config().setRefreshPolicy(config.getRefreshPolicy());
        cache.config().setLoader(config.getLoader());


        boolean protect = config.getPenetrationProtect() != null ? config.getPenetrationProtect()
                : cacheBuilderTemplate.isPenetrationProtect();
        cache.config().setCachePenetrationProtect(protect);
        cache.config().setPenetrationProtectTimeout(config.getPenetrationProtectTimeout());

        for (CacheMonitorInstaller i : cacheBuilderTemplate.getCacheMonitorInstallers()) {
            i.addMonitors(this, cache, config);
        }
        return cache;
    }

    private Cache buildRemote(QuickConfig config) {
        ExternalCacheBuilder cacheBuilder = (ExternalCacheBuilder) cacheBuilderTemplate
                .getCacheBuilder(1, config.getArea());
        if (cacheBuilder == null) {
            throw new CacheConfigException("no remote cache builder: " + config.getArea());
        }

        if (config.getExpire() != null && config.getExpire().toMillis() > 0) {
            cacheBuilder.expireAfterWrite(config.getExpire().toMillis(), TimeUnit.MILLISECONDS);
        }

        String prefix;
        if (config.getUseAreaInPrefix() != null && config.getUseAreaInPrefix()) {
            prefix = config.getArea() + "_" + config.getName();
        } else {
            prefix = config.getName();
        }
        if (cacheBuilder.getConfig().getKeyPrefixSupplier() != null) {
            Supplier<String> supplier = cacheBuilder.getConfig().getKeyPrefixSupplier();
            cacheBuilder.setKeyPrefixSupplier(() -> supplier.get() + prefix);
        } else {
            cacheBuilder.setKeyPrefix(prefix);
        }

        if (config.getKeyConvertor() != null) {
            cacheBuilder.getConfig().setKeyConvertor(config.getKeyConvertor());
        }
        if (config.getValueEncoder() != null) {
            cacheBuilder.getConfig().setValueEncoder(config.getValueEncoder());
        }
        if (config.getValueDecoder() != null) {
            cacheBuilder.getConfig().setValueDecoder(config.getValueDecoder());
        }

        cacheBuilder.setCacheNullValue(config.getCacheNullValue() != null ?
                config.getCacheNullValue() : DEFAULT_CACHE_NULL_VALUE);
        return cacheBuilder.buildCache();
    }

    private Cache buildLocal(QuickConfig config) {
        EmbeddedCacheBuilder cacheBuilder = (EmbeddedCacheBuilder) cacheBuilderTemplate.getCacheBuilder(0, config.getArea());
        if (cacheBuilder == null) {
            throw new CacheConfigException("no local cache builder: " + config.getArea());
        }

        if (config.getLocalLimit() != null && config.getLocalLimit() > 0) {
            cacheBuilder.setLimit(config.getLocalLimit());
        }
        if (config.getCacheType() == CacheType.BOTH &&
                config.getLocalExpire() != null && config.getLocalExpire().toMillis() > 0) {
            cacheBuilder.expireAfterWrite(config.getLocalExpire().toMillis(), TimeUnit.MILLISECONDS);
        } else if (config.getExpire() != null && config.getExpire().toMillis() > 0) {
            cacheBuilder.expireAfterWrite(config.getExpire().toMillis(), TimeUnit.MILLISECONDS);
        }
        if (config.getKeyConvertor() != null) {
            cacheBuilder.getConfig().setKeyConvertor(config.getKeyConvertor());
        }
        cacheBuilder.setCacheNullValue(config.getCacheNullValue() != null ?
                config.getCacheNullValue() : DEFAULT_CACHE_NULL_VALUE);
        return cacheBuilder.buildCache();
    }
}
