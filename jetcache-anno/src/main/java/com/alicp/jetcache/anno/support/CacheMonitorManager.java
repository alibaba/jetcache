/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface CacheMonitorManager {
    void addMonitors(String area, String cacheName, Cache cache);
}
