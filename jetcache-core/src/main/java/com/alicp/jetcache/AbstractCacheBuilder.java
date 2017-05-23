package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractCacheBuilder<T extends AbstractCacheBuilder<T>> implements CacheBuilder, Cloneable {

    protected CacheConfig config;
    private Function<CacheConfig, Cache> buildFunc;

    public CacheConfig getConfig() {
        if (config == null) {
            config = new CacheConfig();
        }
        return config;
    }

    protected T self() {
        return (T) this;
    }

    public T buildFunc(Function<CacheConfig, Cache> buildFunc) {
        this.buildFunc = buildFunc;
        return self();
    }

    protected void beforeBuild() {
    }

    @Deprecated
    public final <K, V> Cache<K, V> build() {
        return buildCache();
    }

    public final <K, V> Cache<K, V> buildCache() {
        if (buildFunc == null) {
            throw new CacheConfigException("no buildFunc");
        }
        beforeBuild();
        CacheConfig c = getConfig().clone();
        Cache<K, V> cache = buildFunc.apply(c);
        if (c.getLoader() != null || c.getBatchLoader() != null) {
            cache = new LoadingCache<>(cache);
        }
        return cache;
    }

    @Override
    public Object clone() {
        AbstractCacheBuilder copy = null;
        try {
            copy = (AbstractCacheBuilder) super.clone();
            copy.config = getConfig().clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    public T keyConvertor(Function<Object, Object> keyConvertor) {
        getConfig().setKeyConvertor(keyConvertor);
        return self();
    }

    public void setKeyConvertor(Function<Object, Object> keyConvertor) {
        getConfig().setKeyConvertor(keyConvertor);
    }

    public T expireAfterAccess(long defaultExpire, TimeUnit timeUnit) {
        getConfig().setExpireAfterAccessInMillis(timeUnit.toMillis(defaultExpire));
        return self();
    }

    public T expireAfterWrite(long defaultExpire, TimeUnit timeUnit) {
        getConfig().setExpireAfterWriteInMillis(timeUnit.toMillis(defaultExpire));
        return self();
    }

    public T cacheNullValueByDefault(boolean cacheNullValueByDefault) {
        getConfig().setCacheNullValueByDefault(cacheNullValueByDefault);
        return self();
    }

    public <K, V> T loader(Function<K, V> loader) {
        getConfig().setLoader(loader);
        return self();
    }

    public <K, V> T batchLoader(Function<K, V> batchLoader) {
        getConfig().setBatchLoader(batchLoader);
        return self();
    }

    public void setExpireAfterWriteInMillis(long expireAfterWriteInMillis) {
        getConfig().setExpireAfterWriteInMillis(expireAfterWriteInMillis);
    }

    public void setExpireAfterAccessInMillis(long expireAfterAccessInMillis) {
        getConfig().setExpireAfterAccessInMillis(expireAfterAccessInMillis);
    }

    public void setCacheNullValueByDefault(boolean cacheNullValueByDefault) {
        getConfig().setCacheNullValueByDefault(cacheNullValueByDefault);
    }

    public <K, V> void setLoader(Function<K, V> loader) {
        getConfig().setLoader(loader);
    }

    public <K, V> void setBatchLoader(Function<K, V> batchLoader) {
        getConfig().setBatchLoader(batchLoader);
    }
}
