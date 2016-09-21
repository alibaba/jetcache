/**
 * Created on  13-09-09 17:29
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.anno.impl.CacheInvokeContext;

import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class GlobalCacheConfig {

    private final Map<String, CacheProvider> providerMap;
    private CacheMonitor cacheMonitor;
    private String[] hidePackages;

    public GlobalCacheConfig(Map<String, CacheProvider> providerMap) {
        this.providerMap = providerMap;
    }

    public CacheInvokeContext createCacheInvokeContext(){
        return new CacheInvokeContext();
    }

    public CacheProvider getCache(String area) {
        CacheProvider cw = providerMap.get(area);
        if (cw == null) {
            throw new CacheException("area " + area + " is not registered");
        }
        return cw;
    }

    public CacheMonitor getCacheMonitor() {
        return cacheMonitor;
    }

    public void setCacheMonitor(CacheMonitor cacheMonitor) {
        this.cacheMonitor = cacheMonitor;
    }

    public String[] getHidePackages() {
        return hidePackages;
    }

    public void setHidePackages(String[] hidePackages) {
        this.hidePackages = hidePackages;
    }
}
