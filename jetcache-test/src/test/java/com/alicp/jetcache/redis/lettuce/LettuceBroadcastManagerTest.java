/**
 * Created on 2022/7/10.
 */
package com.alicp.jetcache.redis.lettuce;

import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.redis.AbstractBroadcastManagerTest;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.Kryo5ValueDecoder;
import com.alicp.jetcache.support.Kryo5ValueEncoder;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static com.alicp.jetcache.redis.lettuce.RedisLettuceCacheTest.checkOS;

/**
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class LettuceBroadcastManagerTest extends AbstractBroadcastManagerTest {

    private void doTest(AbstractRedisClient client, StatefulRedisPubSubConnection pubSubConnection) throws Exception {
        BroadcastManager bm = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .redisClient(client)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(Kryo5ValueEncoder.INSTANCE)
                .valueDecoder(Kryo5ValueDecoder.INSTANCE)
                .keyPrefix(new Random().nextInt() + "")
                .broadcastChannel(LettuceBroadcastManagerTest.class.getSimpleName())
                .pubSubConnection(pubSubConnection)
                .createBroadcastManager(new SimpleCacheManager());
        testBroadcastManager(bm);
    }

    @Test
    public void testSimple() throws Exception {
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        StatefulRedisPubSubConnection pubSubConnection = client.connectPubSub(new JetCacheCodec());
        doTest(client, pubSubConnection);
    }

    @Test
    public void testSentinel() throws Exception {
        RedisURI redisUri = RedisURI.Builder
                .sentinel("127.0.0.1", 26379, "mymaster")
                .withSentinel("127.0.0.1", 26380)
                .withSentinel("127.0.0.1", 26381)
                .build();
        RedisClient client = RedisClient.create(redisUri);
        doTest(client, client.connectPubSub(new JetCacheCodec()));
    }

    @Test
    public void testCluster() throws Exception {
        if (!checkOS()) {
            return;
        }
        RedisURI node1 = RedisURI.create("127.0.0.1", 7000);
        RedisURI node2 = RedisURI.create("127.0.0.1", 7001);
        RedisURI node3 = RedisURI.create("127.0.0.1", 7002);
        RedisClusterClient client = RedisClusterClient.create(Arrays.asList(node1, node2, node3));
        doTest(client, client.connectPubSub(new JetCacheCodec()));
    }
}
