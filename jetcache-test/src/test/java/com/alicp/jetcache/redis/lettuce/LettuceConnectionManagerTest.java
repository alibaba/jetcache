package com.alicp.jetcache.redis.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created on 2017/5/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class LettuceConnectionManagerTest {

    @Test
    public void test() {
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        LettuceConnectionManager m = LettuceConnectionManager.defaultManager();
        m.init(client, null);
        Assert.assertSame(m.commands(client), m.commands(client));
        Assert.assertSame(m.asyncCommands(client), m.asyncCommands(client));
        Assert.assertSame(m.reactiveCommands(client), m.reactiveCommands(client));
        m.removeAndClose(client);
    }

    @Test
    public void testCluster() {
        if (!RedisLettuceCacheTest.checkOS()) {
            return;
        }
        RedisURI node1 = RedisURI.create("127.0.0.1", 7000);
        RedisURI node2 = RedisURI.create("127.0.0.1", 7001);
        RedisURI node3 = RedisURI.create("127.0.0.1", 7002);
        RedisClusterClient client = RedisClusterClient.create(Arrays.asList(node1, node2, node3));
        LettuceConnectionManager m = LettuceConnectionManager.defaultManager();
        m.init(client, null);
        Assert.assertSame(m.commands(client), m.commands(client));
        Assert.assertSame(m.asyncCommands(client), m.asyncCommands(client));
        Assert.assertSame(m.reactiveCommands(client), m.reactiveCommands(client));
        m.removeAndClose(client);
    }
}
