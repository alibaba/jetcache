/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.template;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface CacheMonitorInstaller {
    void addMonitors(CacheManager cacheManager, Cache cache, QuickConfig quickConfig);
}
