/**
 * Created on  13-09-23 09:36
 */
package com.alicp.jetcache.testsupport;

import com.alicp.jetcache.anno.impl.factory.CacheFactory;
import com.alicp.jetcache.support.GlobalCacheConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TestUtil {
    public static GlobalCacheConfig getCacheProviderFactory() {
        MockRemoteCache c = new MockRemoteCache();
        Map<String, CacheFactory> m = new HashMap();
        GlobalCacheConfig f = new GlobalCacheConfig();
        //TODO
        return f;
    }

}
