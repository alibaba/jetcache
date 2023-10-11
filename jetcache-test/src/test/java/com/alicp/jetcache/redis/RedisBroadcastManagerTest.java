/**
 * Created on 2022/5/8.
 */
package com.alicp.jetcache.redis;

import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.support.BroadcastManager;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

/**
 * @author huangli
 */
public class RedisBroadcastManagerTest extends AbstractBroadcastManagerTest {
    @Test
    public void test() throws Exception {
        BroadcastManager manager = RedisCacheBuilder.createRedisCacheBuilder()
                .jedis(new UnifiedJedis(new HostAndPort("127.0.0.1", 6379)))
                .keyPrefix(RedisBroadcastManagerTest.class.getName())
                .broadcastChannel(RedisBroadcastManagerTest.class.getName())
                .createBroadcastManager(new SimpleCacheManager());
        testBroadcastManager(manager);
    }
}
