package com.alicp.jetcache.examples;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.CaffeineCache;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
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
public class MultiLevelCacheExample {
    public static void main(String[] args) {
        Cache<Object, Object> l1Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new CaffeineCache((EmbeddedCacheConfig) c))
                .build();
        Cache<Object, Object> l2Cache = RedisCacheBuilder.createRedisCacheBuilder()
                .jedisPool(/*replace with your jedis pool*/null)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .build();

        MultiLevelCache<Object, Object> multiLevelCache = new MultiLevelCache<>(l1Cache, l2Cache);

        multiLevelCache.put("K1", "V1");
        multiLevelCache.put("K2", "V2", 20, TimeUnit.SECONDS);
        multiLevelCache.get("K1");
        multiLevelCache.invalidate("K2");

    }

}
