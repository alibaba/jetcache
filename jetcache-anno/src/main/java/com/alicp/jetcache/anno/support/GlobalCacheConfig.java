/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.support.StatInfo;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class GlobalCacheConfig {

    private String[] hidePackages;
    private Map<String, CacheBuilder> localCacheBuilders;
    private Map<String, CacheBuilder> remoteCacheBuilders;

    private ConfigProvider configProvider = new SpringConfigProvider();
    protected int statIntervalMinutes = 60;
    protected Consumer<StatInfo> statCallback = null;


    private CacheContext cacheContext;

    public GlobalCacheConfig() {
    }

    @PostConstruct
    public synchronized void init() {
        if (cacheContext == null) {
            cacheContext = configProvider.newContext(this);
            cacheContext.init();
        }
    }

    @PreDestroy
    public synchronized void shutdown() {
        if (cacheContext != null) {
            cacheContext.shutdown();
            cacheContext = null;
        }
    }

    public CacheContext getCacheContext() {
        return cacheContext;
    }

    public String[] getHidePackages() {
        return hidePackages;
    }

    public void setHidePackages(String[] hidePackages) {
        this.hidePackages = hidePackages;
    }

    public Map<String, CacheBuilder> getLocalCacheBuilders() {
        return localCacheBuilders;
    }

    public void setLocalCacheBuilders(Map<String, CacheBuilder> localCacheBuilders) {
        this.localCacheBuilders = localCacheBuilders;
    }

    public Map<String, CacheBuilder> getRemoteCacheBuilders() {
        return remoteCacheBuilders;
    }

    public void setRemoteCacheBuilders(Map<String, CacheBuilder> remoteCacheBuilders) {
        this.remoteCacheBuilders = remoteCacheBuilders;
    }

    public int getStatIntervalMinutes() {
        return statIntervalMinutes;
    }

    public void setStatIntervalMinutes(int statIntervalMinutes) {
        this.statIntervalMinutes = statIntervalMinutes;
    }

    public Consumer<StatInfo> getStatCallback() {
        return statCallback;
    }

    public void setStatCallback(Consumer<StatInfo> statCallback) {
        this.statCallback = statCallback;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }
}
