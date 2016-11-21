package com.alicp.jetcache.anno;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.factory.ExternalCacheFactory;

/**
 * Created on 2016/10/20.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MockRemoteCacheFactory extends ExternalCacheFactory {
    @Override
    public Cache buildCache() {
        return new MockRemoteCache(getConfig());
    }
}
