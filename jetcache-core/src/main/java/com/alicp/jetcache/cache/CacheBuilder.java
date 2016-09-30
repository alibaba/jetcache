package com.alicp.jetcache.cache;

import com.alicp.jetcache.CacheConfigException;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class CacheBuilder<T extends CacheBuilder<T>> {

    protected CacheConfig config;
    private Function<CacheConfig, Cache> buildFunc;

    public static class CacheBuilderImpl extends CacheBuilder<CacheBuilderImpl>{
    }

    public static CacheBuilderImpl createCacheBuilder(){
        return new CacheBuilderImpl();
    }

    protected CacheBuilder() {
    }

    protected CacheConfig getConfig() {
        if (config == null) {
            return new CacheConfig();
        }
        return config;
    }

    protected T self(){
        return (T) this;
    }

    public T withBuildFunc(Function<CacheConfig, Cache> buildFunc){
        this.buildFunc = buildFunc;
        return self();
    }

    protected void beforeBuild(){
    }

    public final <K,V> Cache<K, V> build() {
        if (buildFunc == null) {
            throw new CacheConfigException("no buildFunc");
        }
        beforeBuild();
        return buildFunc.apply(config);
    }

    public T cacheNullValue(boolean cacheNullValue) {
        getConfig().setCacheNullValue(cacheNullValue);
        return self();
    }

    public T defaultExpire(TimeUnit timeUnit, int defaultExpire) {
        getConfig().setDefaultExpireInMillis(timeUnit.toMillis(defaultExpire));
        return self();
    }

    public T keyGenerator(KeyGenerator keyGenerator){
        getConfig().setKeyGenerator(keyGenerator);
        return self();
    }

    public T expireAfterAccess(){
        getConfig().setExpireAfterAccess(true);
        return self();
    }

    public T expireAfterWrite(){
        getConfig().setExpireAfterAccess(false);
        return self();
    }

}
