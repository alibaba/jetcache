/**
 * Created on 2022/5/8.
 */
package com.alicp.jetcache.redis;

import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisBroadcastManagerTest {
    @Test
    public void test() throws Exception {
        BroadcastManager manager = RedisCacheBuilder.createRedisCacheBuilder()
                .jedis(new UnifiedJedis(new HostAndPort("127.0.0.1", 6379)))
                .keyPrefix(RedisBroadcastManagerTest.class.getName())
                .createBroadcastManager();
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
        manager.publish(cm);
        cl.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(cm.getArea(), ar.get().getArea());
        Assertions.assertEquals(cm.getCacheName(), ar.get().getCacheName());
        Assertions.assertArrayEquals(cm.getKeys(), ar.get().getKeys());
        Assertions.assertArrayEquals(cm.getValues(), ar.get().getValues());
        Assertions.assertEquals(cm.getType(), ar.get().getType());
    }
}
