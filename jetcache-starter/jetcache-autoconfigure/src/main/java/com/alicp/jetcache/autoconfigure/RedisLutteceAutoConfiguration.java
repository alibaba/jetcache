package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.redis.luttece.LutteceConnectionManager;
import com.alicp.jetcache.redis.luttece.RedisLutteceCacheBuilder;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 2017/5/10.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Configuration
@Conditional(RedisLutteceAutoConfiguration.RedisLutteceCondition.class)
public class RedisLutteceAutoConfiguration {
    public static final String AUTO_INIT_BEAN_NAME = "redisLutteceAutoInit";

    public static class RedisLutteceCondition extends JetCacheConditon {
        public RedisLutteceCondition() {
            super("redis.luttece");
        }
    }

    @Bean(name = AUTO_INIT_BEAN_NAME)
    public RedisLutteceAutoInit redisLutteceAutoInit() {
        return new RedisLutteceAutoInit();
    }

    public static class RedisLutteceAutoInit extends ExternalCacheAutoInit {

        public RedisLutteceAutoInit() {
            super("redis.luttece");
        }

        @Override
        protected CacheBuilder initCache(RelaxedPropertyResolver resolver, String cacheAreaWithPrefix) {
            Map<String, Object> map = resolver.getSubProperties("uri");
            AbstractRedisClient client = null;
            if (map == null || map.size() == 0) {
                throw new CacheConfigException("uri is required");
            } else if (map.size() == 1) {
                String uri = (String) map.values().iterator().next();
                client = RedisClient.create(uri);
            } else {
                List<RedisURI> list = map.values().stream().map((k) -> RedisURI.create(URI.create(k.toString())))
                        .collect(Collectors.toList());
                client = RedisClusterClient.create(list);
            }

            ExternalCacheBuilder externalCacheBuilder = RedisLutteceCacheBuilder.createRedisLutteceCacheBuilder()
                    .redisClient(client);
            parseGeneralConfig(externalCacheBuilder, resolver);

            // eg: "remote.default.client"
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
