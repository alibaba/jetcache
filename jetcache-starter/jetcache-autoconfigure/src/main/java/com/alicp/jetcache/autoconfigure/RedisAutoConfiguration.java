package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.redis.jedis.JedisClusterCacheBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 2016/11/25.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 *
 * Support jedis cluster mode, updated on 2019/12/12
 * @author <a href="mailto:eason.fengys@gmail.com">fengyingsheng</a>
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 */
@Configuration
@Conditional(RedisAutoConfiguration.RedisJedisCondition.class)
public class RedisAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RedisAutoConfiguration.class);

    public static final String AUTO_INIT_BEAN_NAME = "redisAutoInit";

    enum Mode {
        /**
         * jedis cluster mode.
         */
        CLUSTER("cluster"),

        /**
         * jedis standalone mode.
         */
        STANDALONE("standalone");

        private String name;

        Mode(String name) {
            this.name = name;
        }

        String getName(){
            return this.name;
        }
    }

    @Bean(name = AUTO_INIT_BEAN_NAME)
    public RedisJedisAutoInit redisJedisAutoInit() {
        return new RedisJedisAutoInit();
    }

    public static class RedisJedisCondition extends JetCacheCondition {
        public RedisJedisCondition() {
            super("redis", "redis.jedis");
        }
    }

    public static class RedisJedisAutoInit extends ExternalCacheAutoInit {
        public RedisJedisAutoInit() {
            super("redis", "redis.jedis");
        }

        @Override
        protected CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix) {
            final String mode = ct.getProperty("mode");
            if (Mode.CLUSTER.getName().equals(mode)) {
                return initJedisClusterBuilder(ct, cacheAreaWithPrefix);
            }
            logger.info("init cache area={}, mode=standalone" , cacheAreaWithPrefix.contains(".") ?
                    cacheAreaWithPrefix.split("\\.")[1] : cacheAreaWithPrefix);
            Pool jedisPool = parsePool(ct);
            Pool[] slavesPool = null;
            int[] slavesPoolWeights = null;
            boolean readFromSlave = Boolean.parseBoolean(ct.getProperty("readFromSlave", "False"));
            ConfigTree slaves = ct.subTree("slaves.");
            Set<String> slaveNames = slaves.directChildrenKeys();
            if (slaveNames.size() > 0) {
                slavesPool = new Pool[slaveNames.size()];
                slavesPoolWeights = new int[slaveNames.size()];
                int i = 0;
                for (String slaveName: slaveNames) {
                    ConfigTree slaveConfig = slaves.subTree(slaveName + ".");
                    slavesPool[i] = parsePool(slaveConfig);
                    slavesPoolWeights[i] = Integer.parseInt(slaveConfig.getProperty("weight","100"));
                    i++;
                }
            }

            ExternalCacheBuilder externalCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                    .jedisPool(jedisPool)
                    .readFromSlave(readFromSlave)
                    .jedisSlavePools(slavesPool)
                    .slaveReadWeights(slavesPoolWeights);

            parseGeneralConfig(externalCacheBuilder, ct);

            // eg: "jedisPool.remote.default"
            autoConfigureBeans.getCustomContainer().put("jedisPool." + cacheAreaWithPrefix, jedisPool);

            return externalCacheBuilder;
        }

        private CacheBuilder initJedisClusterBuilder(final ConfigTree ct, final String cacheAreaWithPrefix) {
            logger.info("init cache area {}, mode= cluster" , cacheAreaWithPrefix.contains(".") ?
                    cacheAreaWithPrefix.split("\\.")[1] : cacheAreaWithPrefix);
            final Set<HostAndPort> hostAndPorts = new HashSet<>();
            Map<String, Object> map = ct.subTree("uri").getProperties();
            final List<URI> uriList = map.values().stream().map(k -> URI.create(k.toString()))
                    .collect(Collectors.toList());
            if (uriList != null && !uriList.isEmpty()) {
                for (URI uri : uriList) {
                    HostAndPort hostAndPort = new HostAndPort(uri.getHost(), uri.getPort());
                    hostAndPorts.add(hostAndPort);
                }
            } else {
                throw new CacheConfigException("there is no uri in configuration for jedis cluster.");
            }
            final int timeout = this.getTimeout(ct);
            GenericObjectPoolConfig poolConfig = this.parsePoolConfig(ct);
            String maxAttempts = ct.getProperty("maxAttempts");
            JedisCluster jedisCluster = null;
            if (StringUtils.isEmpty(maxAttempts)) {
                jedisCluster = new JedisCluster(hostAndPorts, timeout, poolConfig);
            } else {
                final int maxAttempt = Integer.parseInt(maxAttempts);
                jedisCluster = new JedisCluster(hostAndPorts, timeout, maxAttempt, poolConfig);
            }
            boolean clusterPipelineEnable = ct.getProperty("clusterPipelineEnable");
            ExternalCacheBuilder externalCacheBuilder = JedisClusterCacheBuilder.createJedisClusterCacheBuilder()
                    .jedisCluster(jedisCluster);
            parseGeneralConfig(externalCacheBuilder, ct);
            autoConfigureBeans.getCustomContainer().put("jedisCluster." + cacheAreaWithPrefix, jedisCluster);
            return externalCacheBuilder;
        }

        private int getTimeout(final ConfigTree ct) {
            return Integer.parseInt(ct.getProperty("timeout", String.valueOf(Protocol.DEFAULT_TIMEOUT)));
        }

        private Pool<Jedis> parsePool(ConfigTree ct) {
            GenericObjectPoolConfig poolConfig = parsePoolConfig(ct);

            String host = ct.getProperty("host", (String) null);
            int port = Integer.parseInt(ct.getProperty("port", "0"));
            int timeout = this.getTimeout(ct);
            String password = ct.getProperty("password", (String) null);
            int database = Integer.parseInt(ct.getProperty("database", String.valueOf(Protocol.DEFAULT_DATABASE)));
            String clientName = ct.getProperty("clientName", (String) null);
            boolean ssl = Boolean.parseBoolean(ct.getProperty("ssl", "false"));

            String masterName = ct.getProperty("masterName", (String) null);
            String sentinels = ct.getProperty("sentinels", (String) null);//ip1:port,ip2:port

            Pool<Jedis> jedisPool;
            if (sentinels == null) {
                Objects.requireNonNull(host, "host/port or sentinels/masterName is required");
                if (port == 0) {
                    throw new IllegalStateException("host/port or sentinels/masterName is required");
                }
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database, clientName, ssl);
            } else {
                Objects.requireNonNull(masterName, "host/port or sentinels/masterName is required");
                String[] strings = sentinels.split(",");
                HashSet<String> sentinelsSet = new HashSet<>();
                for (String s : strings) {
                    if (s != null && !s.trim().equals("")) {
                        sentinelsSet.add(s.trim());
                    }
                }
                jedisPool = new JedisSentinelPool(masterName, sentinelsSet, poolConfig, timeout, password, database, clientName);
            }
            return jedisPool;
        }

        private GenericObjectPoolConfig parsePoolConfig(ConfigTree ct) {
            try {
                // Spring Boot 2.0 removed RelaxedDataBinder class. Binder class not exists in 1.X
                if (ClassUtils.isPresent("org.springframework.boot.context.properties.bind.Binder",
                        this.getClass().getClassLoader())) {
                    // Spring Boot 2.0+
                    String prefix = ct.subTree("poolConfig").getPrefix().toLowerCase();

                    // invoke following code by reflect
                    // Binder binder = Binder.get(environment);
                    // return binder.bind(name, Bindable.of(GenericObjectPoolConfig.class)).get();

                    Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
                    Class<?> bindableClass = Class.forName("org.springframework.boot.context.properties.bind.Bindable");
                    Class<?> bindResultClass = Class.forName("org.springframework.boot.context.properties.bind.BindResult");
                    Method getMethodOnBinder = binderClass.getMethod("get", Environment.class);
                    Method getMethodOnBindResult = bindResultClass.getMethod("get");
                    Method bindMethod = binderClass.getMethod("bind", String.class, bindableClass);
                    Method ofMethod = bindableClass.getMethod("of", Class.class);
                    Object binder = getMethodOnBinder.invoke(null, environment);
                    Object bindable = ofMethod.invoke(null, GenericObjectPoolConfig.class);
                    Object bindResult = bindMethod.invoke(binder, prefix, bindable);
                    return (GenericObjectPoolConfig) getMethodOnBindResult.invoke(bindResult);
                } else {
                    // Spring Boot 1.X
                    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
                    Map<String, Object> props = ct.subTree("poolConfig.").getProperties();

                    // invoke following code by reflect
                    //RelaxedDataBinder binder = new RelaxedDataBinder(poolConfig);
                    //binder.bind(new MutablePropertyValues(props));

                    Class<?> relaxedDataBinderClass = Class.forName("org.springframework.boot.bind.RelaxedDataBinder");
                    Class<?> mutablePropertyValuesClass = Class.forName("org.springframework.beans.MutablePropertyValues");
                    Constructor<?> c1 = relaxedDataBinderClass.getConstructor(Object.class);
                    Constructor<?> c2 = mutablePropertyValuesClass.getConstructor(Map.class);
                    Method bindMethod = relaxedDataBinderClass.getMethod("bind", PropertyValues.class);
                    Object binder = c1.newInstance(poolConfig);
                    bindMethod.invoke(binder, c2.newInstance(props));
                    return poolConfig;
                }
            } catch (Throwable ex) {
                throw new CacheConfigException("parse poolConfig fail", ex);
            }

        }
    }


}
