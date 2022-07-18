/**
 * Created on 2022/07/15.
 */

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.redis.springdata.RedisSpringDataCacheBuilder;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Arrays;

/**
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringDataExample {
    public static void main(String[] args) {
        RedisConnectionFactory factory = lettuceFactory();
        Cache<String, String> cache = RedisSpringDataCacheBuilder.createBuilder()
                .connectionFactory(factory)
                .keyPrefix("myCachePrefix")
                .buildCache();
        cache.put("K1", "V1");
        System.out.println(cache.get("K1"));
    }

    private static RedisConnectionFactory lettuceFactory() {
        LettuceConnectionFactory f = new LettuceConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", 6379));
        f.afterPropertiesSet();
        return f;
    }

    private static RedisConnectionFactory lettuceFactorySentinel() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("127.0.0.1", 26379)
                .sentinel("127.0.0.1", 26380)
                .sentinel("127.0.0.1", 26381);
        LettuceConnectionFactory f = new LettuceConnectionFactory(sentinelConfig);
        f.afterPropertiesSet();
        return f;
    }

    private static RedisConnectionFactory lettuceFactoryCluster() {
        LettuceConnectionFactory f = new LettuceConnectionFactory(
                new RedisClusterConfiguration(Arrays.asList("127.0.0.1:7000","127.0.0.1:7001","127.0.0.1:7002")));
        f.afterPropertiesSet();
        return f;
    }

    private static RedisConnectionFactory jedisFactory() {
        JedisConnectionFactory f = new JedisConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", 6379));
        f.afterPropertiesSet();
        return f;
    }

    private static RedisConnectionFactory jedisFactorySentinel() {
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("127.0.0.1", 26379)
                .sentinel("127.0.0.1", 26380)
                .sentinel("127.0.0.1", 26381);
        JedisConnectionFactory f = new JedisConnectionFactory(sentinelConfig);
        f.afterPropertiesSet();
        return f;
    }

    private static RedisConnectionFactory jedisFactoryCluster() {
        JedisConnectionFactory f = new JedisConnectionFactory(
                new RedisClusterConfiguration(Arrays.asList("127.0.0.1:7000","127.0.0.1:7001","127.0.0.1:7002")));
        f.afterPropertiesSet();
        return f;
    }
}
