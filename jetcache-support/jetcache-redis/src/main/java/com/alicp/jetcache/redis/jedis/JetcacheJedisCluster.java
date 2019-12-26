package com.alicp.jetcache.redis.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisClusterConnectionHandler;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;

/**
 * This class is use for expose {@link JedisClusterConnectionHandler connectionHandler} of {@link JedisCluster}.
 *
 * Created on 2019/12/18.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public class JetcacheJedisCluster extends JedisCluster {

    public JetcacheJedisCluster(final HostAndPort node, final int timeout, final GenericObjectPoolConfig poolConfig) {
        super(node, timeout, poolConfig);

    }

    public JetcacheJedisCluster(final HostAndPort node, final int timeout, final int maxAttempts,
            final GenericObjectPoolConfig poolConfig) {
        super(node, timeout, maxAttempts, poolConfig);
    }

    JedisSlotBasedConnectionHandler getConnectionHandler() {
        return (JedisSlotBasedConnectionHandler) connectionHandler;
    }
}
