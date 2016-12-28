package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.util.Map;

/**
 * Created on 2016/11/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@Conditional(RedisAutoConfiguration.RedisCondition.class)
public class RedisAutoConfiguration extends ExternalCacheAutoConfiguration {

    public RedisAutoConfiguration() {
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

        String host = r.getRequiredProperty("host");
        int port = Integer.parseInt(r.getRequiredProperty("port"));
        int timeout = Integer.parseInt(r.getProperty("timeout", String.valueOf(Protocol.DEFAULT_TIMEOUT)));
        String password = r.getProperty("password", (String) null);
        int database = Integer.parseInt(r.getProperty("database", String.valueOf(Protocol.DEFAULT_DATABASE)));
        String clientName = r.getProperty("clientName", (String) null);
        boolean ssl = Boolean.parseBoolean(r.getProperty("ssl", "false"));

        JedisPool jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database, clientName, ssl);


        ExternalCacheBuilder externalCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                .jedisPool(jedisPool);
        parseGeneralConfig(externalCacheBuilder, r);

        autoConfigureBeans.getCustomContainer().put("jedisPool." + cacheAreaWithPrefix, jedisPool);

        return externalCacheBuilder;
    }

    public static class RedisCondition extends JetCacheConditon {
        public RedisCondition() {
            super("redis");
        }
    }


}
