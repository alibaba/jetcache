package com.alicp.jetcache.redis.luttece;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created on 2017/5/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class LutteceConnectionManagerTest {

    @Test
    public void test() {
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        LutteceConnectionManager m = LutteceConnectionManager.defaultManager();
        Assert.assertSame(m.commands(client), m.commands(client));
        Assert.assertSame(m.asyncCommands(client), m.asyncCommands(client));
        Assert.assertSame(m.reactiveCommands(client), m.reactiveCommands(client));
        m.removeAndClose(client);
    }

    @Test
    public void testCluster() {
        if (!RedisLutteceCacheTest.checkOS()) {
            return;
        }
        RedisURI node1 = RedisURI.create("127.0.0.1", 7000);
        RedisURI node2 = RedisURI.create("127.0.0.1", 7001);
        RedisURI node3 = RedisURI.create("127.0.0.1", 7002);
        RedisClusterClient client = RedisClusterClient.create(Arrays.asList(node1, node2, node3));
        LutteceConnectionManager m = LutteceConnectionManager.defaultManager();
        Assert.assertSame(m.commands(client), m.commands(client));
        Assert.assertSame(m.asyncCommands(client), m.asyncCommands(client));
        Assert.assertSame(m.reactiveCommands(client), m.reactiveCommands(client));
        m.removeAndClose(client);
    }
}
