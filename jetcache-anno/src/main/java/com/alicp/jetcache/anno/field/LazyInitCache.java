package com.alicp.jetcache.anno.field;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.method.ClassUtil;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/12/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class LazyInitCache implements Cache {

    private transient boolean inited;
    private Cache cache;

    private ConfigurableListableBeanFactory beanFactory;
    private CreateCache ann;
    private Field field;

    public LazyInitCache(ConfigurableListableBeanFactory beanFactory, CreateCache ann, Field field) {
        this.beanFactory = beanFactory;
        this.ann = ann;
        this.field = field;
    }

    private void checkInit() {
        if (!inited) {
            synchronized (this) {
                if (!inited) {
                    init();
                    inited = true;
                }
            }
        }
    }

    private void init() {
        if (inited) {
            throw new IllegalStateException();
        }
        GlobalCacheConfig globalCacheConfig = beanFactory.getBean(GlobalCacheConfig.class);

        CacheAnnoConfig cac = new CacheAnnoConfig();
        cac.setArea(ann.area());
        cac.setName(ann.name());
        cac.setExpire(ann.expire());
        cac.setCacheType(ann.cacheType());
        cac.setLocalLimit(ann.localLimit());
        cac.setVersion(ann.version());
        cac.setSerialPolicy(ann.serialPolicy());
        cac.setKeyConvertor(ann.keyConvertor());

        String cacheName = cac.getName();
        if (CacheConsts.DEFAULT_NAME.equalsIgnoreCase(cacheName)) {
            StringBuilder sb = new StringBuilder();
            sb.append(field.getDeclaringClass().getName());
            sb.append(".").append(field.getName());
            ClassUtil.removeHiddenPackage(globalCacheConfig.getHiddenPackages(), sb);
            cacheName = sb.toString();
        }
        if (cac.getVersion() != CacheConsts.DEFAULT_VERSION) {
            cacheName = cac.getVersion() + "_" + cacheName;
        }
        String fullCacheName = cac.getArea() + "_" + cacheName;
        cache = globalCacheConfig.getCacheContext().__createOrGetCache(cac, ann.area(), fullCacheName);
    }

    @Override
    public CacheConfig config() {
        checkInit();
        return cache.config();
    }

    @Override
    public Object get(Object key) {
        checkInit();
        return cache.get(key);
    }

    @Override
    public CacheGetResult GET(Object key) {
        checkInit();
        return cache.GET(key);
    }

    @Override
    public Object computeIfAbsent(Object key, Function loader) {
        checkInit();
        return cache.computeIfAbsent(key, loader);
    }

    @Override
    public Object computeIfAbsent(Object key, Function loader, boolean cacheNullWhenLoaderReturnNull) {
        checkInit();
        return cache.computeIfAbsent(key, loader, cacheNullWhenLoaderReturnNull);
    }

    @Override
    public Object computeIfAbsent(Object key, Function loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit) {
        checkInit();
        return cache.computeIfAbsent(key, loader, cacheNullWhenLoaderReturnNull, expire, timeUnit);
    }

    @Override
    public void put(Object key, Object value) {
        checkInit();
        cache.put(key, value);
    }

    @Override
    public void put(Object key, Object value, long expire, TimeUnit timeUnit) {
        checkInit();
        cache.put(key, value, expire, timeUnit);
    }

    @Override
    public CacheResult PUT(Object key, Object value) {
        checkInit();
        return cache.PUT(key, value);
    }

    @Override
    public CacheResult PUT(Object key, Object value, long expire, TimeUnit timeUnit) {
        checkInit();
        return cache.PUT(key, value, expire, timeUnit);
    }

    @Override
    public void invalidate(Object key) {
        checkInit();
        cache.invalidate(key);
    }

    @Override
    public CacheResult INVALIDATE(Object key) {
        checkInit();
        return INVALIDATE(key);
    }

}
