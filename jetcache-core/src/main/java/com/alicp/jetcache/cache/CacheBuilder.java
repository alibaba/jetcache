package com.alicp.jetcache.cache;

import com.alicp.jetcache.CacheConfigException;

import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class CacheBuilder<T extends CacheBuilder<T>> {

    protected CacheBuilderConfig config;
    private Function<CacheBuilderConfig, Cache> buildFunc;

    public static class CacheBuilderImpl extends CacheBuilder<CacheBuilderImpl>{
    }

    public static CacheBuilderImpl createCacheBuilder(){
        return new CacheBuilderImpl();
    }

    protected CacheBuilder() {
    }

    protected CacheBuilderConfig getConfig() {
        if (config == null) {
            return new CacheBuilderConfig();
        }
        return config;
    }

    protected T self(){
        return (T) this;
    }

    public T withBuildFunc(Function<CacheBuilderConfig, Cache> buildFunc){
        this.buildFunc = buildFunc;
        return self();
    }

    protected void beforeBuild(){
    }

    public final Cache build() {
        if (getConfig().getSubArea() == null) {
            throw new CacheConfigException("no subArea");
        }
        if (buildFunc == null) {
            throw new CacheConfigException("no buildFunc");
        }
        beforeBuild();
        return buildFunc.apply(config);
    }

    public T withSubArea(String subArea) {
        getConfig().setSubArea(subArea);
        return self();
    }

    public T withCacheNullValue(boolean cacheNullValue) {
        getConfig().setCacheNullValue(cacheNullValue);
        return self();
    }

    public T withDefaultTtlInSeconds(int defaultTtlInSeconds) {
        getConfig().setDefaultTtlInSeconds(defaultTtlInSeconds);
        return self();
    }

}
