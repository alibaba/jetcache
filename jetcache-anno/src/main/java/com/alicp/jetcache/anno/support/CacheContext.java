/**
 * Created on  13-09-04 15:34
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.anno.impl.CacheInvokeContext;
import com.alicp.jetcache.anno.impl.ClassUtil;
import com.alicp.jetcache.factory.EmbeddedCacheFactory;
import com.alicp.jetcache.factory.ExternalCacheFactory;
import com.alicp.jetcache.support.*;

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

    public CacheContext(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
        this.cacheManager = new CacheManager();
    }

    public CacheInvokeContext createCacheInvokeContext() {
        CacheInvokeContext c = newCacheInvokeContext();
        c.setCacheFunction((invokeContext) -> {
            CacheAnnoConfig cacheAnnoConfig = invokeContext.getCacheInvokeConfig().getCacheAnnoConfig();
            String area = cacheAnnoConfig.getArea();
            String subArea = ClassUtil.getSubArea(cacheAnnoConfig.getVersion(),
                    invokeContext.getMethod(), invokeContext.getHiddenPackages());
            String cacheName = area + "_" + subArea;
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                if (cacheAnnoConfig.getCacheType() == CacheType.LOCAL) {
                    cache = buildLocal(cacheAnnoConfig, area);
                } else if (cacheAnnoConfig.getCacheType() == CacheType.REMOTE) {
                    cache = buildRemote(cacheAnnoConfig, area, subArea);
                } else {
                    Cache local = buildLocal(cacheAnnoConfig, area);
                    Cache remote = buildRemote(cacheAnnoConfig, area, subArea);
                    cache = new MultiLevelCache(local, remote);
                    cacheManager.addCache(cacheName + "_local", local);
                    cacheManager.addCache(cacheName + "_remote", remote);
                }
                cacheManager.addCache(cacheName, cache);
            }
            return cache;
        });

        return c;
    }

    private Cache buildRemote(CacheAnnoConfig cacheAnnoConfig, String area, String subArea) {
        ExternalCacheFactory cacheFactory = (ExternalCacheFactory) globalCacheConfig.getRemoteCacheFacotories().get(area);
        if (cacheFactory == null) {
            throw new CacheConfigException("no CacheFactory with name \"" + area + "\" defined in remoteCacheFacotories");
        }
        cacheFactory.setDefaultExpireInMillis(cacheAnnoConfig.getExpire() * 1000);
        cacheFactory.setKeyPrefix(subArea);
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
        EmbeddedCacheFactory cacheFactory = (EmbeddedCacheFactory) globalCacheConfig.getLocalCacheFacotories().get(area);
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
