/**
 * Created on  13-09-09 17:29
 */
package com.taobao.geek.cache;

import java.util.Map;

/**
 * @author yeli.hl
 */
public class CacheFactory {

    private final Map<String, CacheWapper> cacheMap;

    public CacheFactory(Map<String, CacheWapper> cacheMap) {
        this.cacheMap = cacheMap;
    }

    public CacheWapper getCache(String area) {
        CacheWapper cw = cacheMap.get(area);
        if (cw == null) {
            throw new CacheException("area " + area + " is not registered");
        }
        return cw;
    }

}
