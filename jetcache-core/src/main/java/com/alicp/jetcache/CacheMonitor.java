package com.alicp.jetcache;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface CacheMonitor {

    void afterGET(long millis, Object key, CacheGetResult result);

    void afterPUT(long millis, Object key, Object value, CacheResult result);

    void afterINVALIDATE(long millis, Object key, CacheResult result);

    void afterLoad(long millis, Object key, Object loadedValue, boolean success);
}
