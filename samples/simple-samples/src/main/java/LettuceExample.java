/**
 * Created on 2022/07/18.
 */

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import io.lettuce.core.RedisClient;

/**
 * @author huangli
 */
public class LettuceExample {
    public static void main(String[] args) {
        Cache<String, String> cache = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .redisClient(RedisClient.create("redis://127.0.0.1"))
                .keyPrefix("projectB")
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .buildCache();
        cache.put("K1", "V1");
        System.out.println(cache.get("K1"));
    }

}
