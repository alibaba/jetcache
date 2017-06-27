package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * Created on 2016/11/25.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Configuration
@Conditional(RedisAutoConfiguration.RedisCondition.class)
public class RedisAutoConfiguration {

    public static final String AUTO_INIT_BEAN_NAME = "redisAutoInit";

    @Bean(name = AUTO_INIT_BEAN_NAME)
    public RedisAutoInit redisAutoInit(){
        return new RedisAutoInit();
    }

    public static class RedisCondition extends JetCacheConditon {
        public RedisCondition() {
            super("redis");
        }
    }

    public static class RedisAutoInit extends ExternalCacheAutoInit {
        public RedisAutoInit() {
            super("redis");
        }

        @Autowired
        private AutoConfigureBeans autoConfigureBeans;

        @Override
        protected CacheBuilder initCache(RelaxedPropertyResolver r, String cacheAreaWithPrefix) {

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            Map<String, Object> props = r.getSubProperties("poolConfig");
            RelaxedDataBinder binder = new RelaxedDataBinder(poolConfig);
            binder.bind(new MutablePropertyValues(props));

            String host = r.getProperty("host", (String)null);
            int port = Integer.parseInt(r.getProperty("port", "0"));
            int timeout = Integer.parseInt(r.getProperty("timeout", String.valueOf(Protocol.DEFAULT_TIMEOUT)));
            String password = r.getProperty("password", (String) null);
            int database = Integer.parseInt(r.getProperty("database", String.valueOf(Protocol.DEFAULT_DATABASE)));
            String clientName = r.getProperty("clientName", (String) null);
            boolean ssl = Boolean.parseBoolean(r.getProperty("ssl", "false"));

            String masterName = r.getProperty("masterName", (String) null);
            String sentinels = r.getProperty("sentinels", (String) null);//ip1:port,ip2:port

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


            ExternalCacheBuilder externalCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                    .jedisPool(jedisPool);
            parseGeneralConfig(externalCacheBuilder, r);

            // eg: "jedisPool.remote.default"
            autoConfigureBeans.getCustomContainer().put("jedisPool." + cacheAreaWithPrefix, jedisPool);

            return externalCacheBuilder;
        }
    }


}
