/**
 * Created on  13-09-23 09:36
 */
package com.alicp.jetcache.anno.spring;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TestUtil {
    public static GlobalCacheConfig getCacheProviderFactory() {
        MockRemoteCache c = new MockRemoteCache();
        GlobalCacheConfig f = new GlobalCacheConfig();
//        f.setCacheManager();
        return f;
    }

}
