package com.alicp.jetcache;

import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.KryoValueDecoder;
import com.alicp.jetcache.support.KryoValueEncoder;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/9/27.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class Example {
    public static void main(String[] args) {
        Cache<Object, Object> l1Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(10)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        Cache<Object, Object> l2Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100000)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .softValues()
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        Cache<Object, Object> l3Cache = RedisCacheBuilder.createRedisCacheBuilder()
                .jedisPool(/*replace with your jedis pool*/null)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .build();

        MultiLevelCache<Object, Object> multiLevelCache = new MultiLevelCache<>(l1Cache, l2Cache, l3Cache);

        multiLevelCache.put("K1", "V1");
        multiLevelCache.put("K2", "V2", 20, TimeUnit.SECONDS);
        multiLevelCache.get("K1");
        multiLevelCache.invalidate("K2");

        DynamicQuery key = new DynamicQuery();
        key.setName("AAA");
        key.setEmail("BBB");
        multiLevelCache.get(key);

        multiLevelCache.computeIfAbsent("K3", k -> loadFromDatabase(k));
    }

    private static Object loadFromDatabase(Object key) {
        //...
        return null;
    }

    // no "equals" method
    static class DynamicQuery {
        private long id;
        private String name;
        private String email;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
