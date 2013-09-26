/**
 * Created on  13-09-23 09:36
 */
package com.taobao.geek.jetcache.support;

import com.taobao.geek.jetcache.CacheProvider;
import com.taobao.geek.jetcache.CacheProviderFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TestUtil {
    public static CacheProviderFactory getCacheProviderFactory() {
        MockRemoteCache c = new MockRemoteCache();
        CacheProvider p = new CacheProvider();
        p.setRemoteCache(c);
        Map<String, CacheProvider> m = new HashMap<String, CacheProvider>();
        m.put("", p);
        CacheProviderFactory f = new CacheProviderFactory(m);
        return f;
    }

}
