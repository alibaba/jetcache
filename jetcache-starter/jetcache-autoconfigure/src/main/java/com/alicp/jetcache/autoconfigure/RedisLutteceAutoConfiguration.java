package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.redis.luttece.LutteceConnectionManager;
import com.alicp.jetcache.redis.luttece.RedisLutteceCacheBuilder;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * Created on 2017/5/10.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@Conditional(RedisLutteceAutoConfiguration.RedisLutteceCondition.class)
public class RedisLutteceAutoConfiguration {
    public static class RedisLutteceCondition extends JetCacheConditon {
        public RedisLutteceCondition() {
            super("redis.luttece");
        }
    }

    @Bean(name="redisLutteceAutoInit")
    public RedisLutteceAutoInit redisLutteceAutoInit(){
        return new RedisLutteceAutoInit();
    }

    public static class RedisLutteceAutoInit extends ExternalCacheAutoInit {

        public RedisLutteceAutoInit() {
            super("redis.luttece");
        }

        @Override
        protected CacheBuilder initCache(RelaxedPropertyResolver resolver, String cacheAreaWithPrefix) {
            String uriStr = resolver.getRequiredProperty("uri");
            URI uri = URI.create(uriStr);
            String clusterScheme = "redis-cluster";
            AbstractRedisClient client = null;
            if (uri.getScheme().equals(clusterScheme)) {
                uriStr = clusterScheme + uriStr.substring(clusterScheme.length());
                client = RedisClusterClient.create(uriStr);
            } else {
                client = RedisClient.create(uriStr);
            }

            ExternalCacheBuilder externalCacheBuilder = RedisLutteceCacheBuilder.createRedisLutteceCacheBuilder()
                    .redisClient(client);
            parseGeneralConfig(externalCacheBuilder, resolver);

            // eg: "jedisPool.remote.default"
            autoConfigureBeans.getCustomContainer().put(cacheAreaWithPrefix + ".client", client);
            LutteceConnectionManager m = LutteceConnectionManager.defaultManager();
            autoConfigureBeans.getCustomContainer().put(cacheAreaWithPrefix + ".connection", m.connection(client));
            autoConfigureBeans.getCustomContainer().put(cacheAreaWithPrefix + ".commands", m.commands(client));
            autoConfigureBeans.getCustomContainer().put(cacheAreaWithPrefix + ".asyncCommands", m.asyncCommands(client));
            autoConfigureBeans.getCustomContainer().put(cacheAreaWithPrefix + ".reactiveCommands", m.reactiveCommands(client));
            return externalCacheBuilder;
        }
    }
}
