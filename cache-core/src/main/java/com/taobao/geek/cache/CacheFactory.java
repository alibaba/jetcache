/**
 * Created on  13-09-09 17:29
 */
package com.taobao.geek.cache;

import java.util.Map;

/**
 * @author yeli.hl
 */
public class CacheFactory {

    private final Map<String, CacheProvider> cacheMap;

    public CacheFactory(Map<String, CacheProvider> cacheMap) {
        this.cacheMap = cacheMap;
    }

    public CacheProvider getCache(String area) {
        CacheProvider cw = cacheMap.get(area);
        if (cw == null) {
            throw new CacheException("area " + area + " is not registered");
        }
        return cw;
    }

}
