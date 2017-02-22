package com.alicp.jetcache;

import com.alicp.jetcache.event.CacheEvent;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@FunctionalInterface
public interface CacheMonitor {

    void afterOpetation(CacheEvent event);

//    void afterGET(long millis, Object key, CacheGetResult result);
//
//    void afterPUT(long millis, Object key, Object value, CacheResult result);
//
//    void afterREMOVE(long millis, Object key, CacheResult result);
//
//    void afterLoad(long millis, Object key, Object loadedValue, boolean success);
}
