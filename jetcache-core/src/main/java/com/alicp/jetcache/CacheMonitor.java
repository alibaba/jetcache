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

}
