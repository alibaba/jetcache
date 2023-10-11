/**
 * Created on 2022/07/10.
 */
package com.alicp.jetcache.redis;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.MultiLevelCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.Assertions;

/**
 * @author huangli
 */
public class AbstractBroadcastManagerTest {
    protected void testBroadcastManager(BroadcastManager manager) throws Exception {
        CacheMessage cm = new CacheMessage();
        cm.setArea("area");
        cm.setCacheName("cacheName");
        cm.setKeys(new String[]{"K"});
        cm.setValues(new String[]{"V1"});
        cm.setType(100);

        Cache c1 = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder().buildCache();
        Cache c2 = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder().buildCache();
        MultiLevelCache mc = (MultiLevelCache) MultiLevelCacheBuilder
                .createMultiLevelCacheBuilder()
                .addCache(c1, c2)
                .buildCache();
        mc.put("K", "V1");
        Assertions.assertEquals("V1", c1.get("K"));
        manager.getCacheManager().putCache("area", "cacheName", mc);

        manager.startSubscribe();
        Thread.sleep(50);
        CacheResult result = manager.publish(cm);
        Assertions.assertTrue(result.isSuccess());

        TestUtil.waitUtil(() -> c1.get("K") == null);

        manager.close();
    }
}
