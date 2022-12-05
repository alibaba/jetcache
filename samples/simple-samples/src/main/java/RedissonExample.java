/**
 * Created on 2022/11/11.
 */

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.redisson.RedissonCacheBuilder;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import org.redisson.Redisson;
import org.redisson.config.Config;

/**
 * @author <a href="mailto:etczyp@qq.com">zhangyaoping</a>
 */

public class RedissonExample {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        Cache<String, String> cache = RedissonCacheBuilder.createBuilder()
                .redissonClient(Redisson.create(config))
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .keyPrefix("projectC")
                .buildCache();
        cache.put("K1", "V1");
        System.out.println(cache.get("K1"));
    }

}
