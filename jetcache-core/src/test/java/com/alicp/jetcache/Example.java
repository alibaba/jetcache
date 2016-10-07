package com.alicp.jetcache;

import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;

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
                .build();
        Cache<String, String> l2Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(2000)
                .defaultExpire(200, TimeUnit.SECONDS)
                .build();



        CompoundCache<String, String> compoundCache = new CompoundCache<>(l1Cache, l2Cache);
    }
}
