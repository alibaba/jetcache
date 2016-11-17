package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractCacheBuilder<T extends AbstractCacheBuilder<T>> implements CacheBuilder {

    protected CacheConfig config;
    private Function<CacheConfig, Cache> buildFunc;

    public static class CacheBuilderImpl extends AbstractCacheBuilder<CacheBuilderImpl> {
    }

    public static CacheBuilderImpl createCacheBuilder() {
        return new CacheBuilderImpl();
    }

    protected AbstractCacheBuilder() {
    }

    protected CacheConfig getConfig() {
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
        return buildFunc.apply(getConfig().clone());
    }

    public T keyConvertor(Function<Object, Object> keyConvertor) {
        getConfig().setKeyConvertor(keyConvertor);
        return self();
    }

    public T expireAfterAccess(int defaultExpire, TimeUnit timeUnit) {
        getConfig().setDefaultExpireInMillis(timeUnit.toMillis(defaultExpire));
        getConfig().setExpireAfterAccess(true);
        return self();
    }

    public T expireAfterWrite(int defaultExpire, TimeUnit timeUnit) {
        getConfig().setDefaultExpireInMillis(timeUnit.toMillis(defaultExpire));
        getConfig().setExpireAfterAccess(false);
        return self();
    }

}
