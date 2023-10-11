/**
 * Created on 2022/07/18.
 */

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

/**
 * @author huangli
 */
public class JedisExample {
    public static void main(String[] args) {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPool pool = new JedisPool(pc, "127.0.0.1", 6379);
        Cache<String, String> cache = RedisCacheBuilder.createRedisCacheBuilder()
                .jedisPool(pool)
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .keyPrefix("projectA")
                .buildCache();
        cache.put("K1", "V1");
        System.out.println(cache.get("K1"));
    }

}
