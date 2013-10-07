/**
 * Created on  13-09-09 17:29
 */
package com.taobao.geek.jetcache;

import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class GlobalCacheConfig {

    private final Map<String, CacheProvider> providerMap;
    private CacheMonitor cacheMonitor;

    public GlobalCacheConfig(Map<String, CacheProvider> providerMap) {
        this.providerMap = providerMap;
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
}
