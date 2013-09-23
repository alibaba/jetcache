/**
 * Created on  13-09-09 17:29
 */
package com.taobao.geek.jetcache;

import java.util.Map;

/**
 * @author yeli.hl
 */
public class CacheProviderFactory {

    private final Map<String, CacheProvider> providerMap;

    public CacheProviderFactory(Map<String, CacheProvider> providerMap) {
        this.providerMap = providerMap;
    }

    public CacheProvider getCache(String area) {
        CacheProvider cw = providerMap.get(area);
        if (cw == null) {
            throw new CacheException("area " + area + " is not registered");
        }
        return cw;
    }

}
