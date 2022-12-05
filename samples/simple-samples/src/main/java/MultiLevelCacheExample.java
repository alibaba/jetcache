import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MultiLevelCacheBuilder;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/9/27.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MultiLevelCacheExample {
    public static void main(String[] args) {
        Cache<Object, Object> l1Cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .buildCache();

        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPool pool = new JedisPool(pc, "127.0.0.1", 6379);
        Cache<Object, Object> l2Cache = RedisCacheBuilder.createRedisCacheBuilder()
                .jedisPool(pool)
                .keyPrefix("projectD")
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .buildCache();

        Cache<Object, Object> multiLevelCache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(l1Cache, l2Cache)
                .buildCache();

        multiLevelCache.put("K1", "V1");
        multiLevelCache.put("K2", "V2", 20, TimeUnit.SECONDS);
        System.out.println(multiLevelCache.get("K1"));
        multiLevelCache.remove("K2");

    }

}
