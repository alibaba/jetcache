package com.alicp.jetcache.factory;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCacheFactory extends EmbeddedCacheFactory {
    @Override
    public Cache buildCache() {
        return new LinkedHashMapCache((EmbeddedCacheConfig) getConfig().clone());
    }

}
