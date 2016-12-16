package com.alicp.jetcache;

import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.test.AbstractCacheTest;
import org.junit.Test;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MonitoredCacheTest extends AbstractCacheTest {
    @Test
    public void test(){
        Cache target = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
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
