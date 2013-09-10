/**
 * Created on  13-09-10 14:42
 */
package com.taobao.geek.cache;

/**
 * @author yeli.hl
 */
public interface CacheWapper {
    public Cache getRemoteCache();
    public Cache getLocalCache();
}
