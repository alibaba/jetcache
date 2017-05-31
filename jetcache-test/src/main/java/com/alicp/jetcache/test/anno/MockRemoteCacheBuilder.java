package com.alicp.jetcache.test.anno;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheConfig;

/**
 * Created on 2016/10/20.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MockRemoteCacheBuilder extends ExternalCacheBuilder {
    public MockRemoteCacheBuilder() {
        this.setKeyPrefix("DEFAULT_PREFIX");
        buildFunc((c) -> new MockRemoteCache((ExternalCacheConfig) c));
    }
}
