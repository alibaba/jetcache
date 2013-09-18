/**
 * Created on  13-09-09 17:28
 */
package com.taobao.geek.jetcache;

/**
 * @author yeli.hl
 */
public interface Cache {

    public CacheResult get(CacheConfig cacheConfig, String subArea, String key);

    public CacheResultCode put(CacheConfig cacheConfig, String subArea, String key, Object value);
}
