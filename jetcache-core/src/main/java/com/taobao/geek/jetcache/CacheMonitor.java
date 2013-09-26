/**
 * Created on  13-09-26 09:42
 */
package com.taobao.geek.jetcache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface CacheMonitor {
    public void onGet(CacheConfig cacheConfig, String subArea, String key, boolean localHit, boolean remoteHit);
}
