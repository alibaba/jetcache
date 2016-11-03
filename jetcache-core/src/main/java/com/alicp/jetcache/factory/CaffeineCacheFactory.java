package com.alicp.jetcache.factory;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCache;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CaffeineCacheFactory extends EmbeddedCacheFactory {
    @Override
    public Cache buildCache() {
        return new CaffeineCache((EmbeddedCacheConfig) getConfig().clone());
    }

}
