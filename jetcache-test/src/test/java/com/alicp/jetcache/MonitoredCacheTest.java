package com.alicp.jetcache;

import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.test.AbstractCacheTest;
import org.junit.Test;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MonitoredCacheTest extends AbstractCacheTest {
    @Test
    public void test() throws Exception {
        Cache target = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .buildCache();
        cache = new MonitoredCache(target, event -> {});

        baseTest();
    }
}
