package com.alicp.jetcache;

import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import org.junit.Test;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MonitoredCacheTest extends AbstractCacheTest {
    @Test
    public void test(){
        Cache target = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .buildCache();
        cache = new MonitoredCache(target, new CacheMonitor() {
            @Override
            public void afterGET(long millis, Object key, CacheGetResult result) {

            }

            @Override
            public void afterPUT(long millis, Object key, Object value, CacheResult result) {

            }

            @Override
            public void afterINVALIDATE(long millis, Object key, CacheResult result) {

            }

            @Override
            public void afterLoad(long millis, Object key, Object loadedValue, boolean success) {

            }
        });

        baseTest();
    }
}
