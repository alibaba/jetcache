/**
 * Created on 2022/07/10.
 */
package com.alicp.jetcache.redis;

import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class AbstractBroadcastManagerTest {
    protected void testBroadcastManager(BroadcastManager manager) throws InterruptedException {
        CacheMessage cm = new CacheMessage();
        cm.setArea("area");
        cm.setCacheName("cacheName");
        cm.setKeys(new String[]{"1"});
        cm.setValues(new String[]{"2"});
        cm.setType(100);
        AtomicReference<CacheMessage> ar = new AtomicReference<>();
        CountDownLatch cl = new CountDownLatch(1);
        manager.startSubscribe(m -> {
            ar.set(m);
            cl.countDown();
        });
        CacheResult result = manager.publish(cm);
        cl.await(1, TimeUnit.SECONDS);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertNotNull(ar.get());
        Assertions.assertEquals(cm.getArea(), ar.get().getArea());
        Assertions.assertEquals(cm.getCacheName(), ar.get().getCacheName());
        Assertions.assertArrayEquals(cm.getKeys(), ar.get().getKeys());
        Assertions.assertArrayEquals(cm.getValues(), ar.get().getValues());
        Assertions.assertEquals(cm.getType(), ar.get().getType());
    }
}
