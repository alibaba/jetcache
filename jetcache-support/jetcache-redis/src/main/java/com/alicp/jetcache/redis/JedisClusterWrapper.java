package com.alicp.jetcache.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.ClusterPipeline;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.providers.ClusterConnectionProvider;

import java.util.Set;

/**
 * Support for pipeline in JedisCluster.
 */
public class JedisClusterWrapper extends JedisCluster {

    private final ClusterConnectionProvider provider;

    public JedisClusterWrapper(Set<HostAndPort> hostAndPortSet, int connectionTimeout, int soTimeout, int maxAttempt, String user, String password, String clientName, GenericObjectPoolConfig poolConfig, boolean ssl) {
        super(hostAndPortSet, connectionTimeout, soTimeout, maxAttempt, user, password, clientName, poolConfig, ssl);
        DefaultJedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder().connectionTimeoutMillis(connectionTimeout)
                .socketTimeoutMillis(soTimeout).user(user).password(password).clientName(clientName).ssl(ssl).build();
        this.provider = new ClusterConnectionProvider(hostAndPortSet, jedisClientConfig, poolConfig);
    }

    /**
     * only for unit test
     * @param hostAndPortSet
     */
    public JedisClusterWrapper(Set<HostAndPort> hostAndPortSet) {
        super(hostAndPortSet);
        DefaultJedisClientConfig defaultConfig = DefaultJedisClientConfig.builder().build();
        this.provider = new ClusterConnectionProvider(hostAndPortSet, defaultConfig);
    }

    public ClusterPipeline getPipeline() {
        return new ClusterPipeline(provider);
    }
}
