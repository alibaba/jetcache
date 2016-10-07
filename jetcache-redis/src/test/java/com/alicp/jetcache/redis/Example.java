package com.alicp.jetcache.redis;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CompoundCache;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
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
        Cache<String, String> l1Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(10)
                .defaultExpire(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .softValues()
                .expireAfterWrite()
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        Cache<String, String> l2Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100000)
                .defaultExpire(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .softValues()
                .expireAfterWrite()
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        Cache<String, String> l3Cache = RedisCacheBuilder.createRedisCacheBuilder()
                .jedisPool(/*replace with your jedis pool*/null)
                .defaultExpire(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(KryoValueEncoder.INSTANCE)
                .valueDecoder(KryoValueDecoder.INSTANCE)
                .build();

        CompoundCache<String, String> compoundCache = new CompoundCache<>(l1Cache, l2Cache, l3Cache);

        compoundCache.put("K1", "V1");
        compoundCache.put("K2", "V2", 20, TimeUnit.SECONDS);
        compoundCache.get("K1");
        compoundCache.invalidate("K2");

    }
}
