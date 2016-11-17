package com.alicp.jetcache.examples;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCache;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/2.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SimpleCacheExample {
    public static void main(String[] args) {
        Cache<String, Integer> cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
//                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .buildFunc(c -> new CaffeineCache((EmbeddedCacheConfig) c))
                .buildCache();
        cache.put("20161111", 1000000, 1 ,TimeUnit.HOURS);
        Integer orderCount1 = cache.get("20161111");
        Integer orderCount2 = cache.computeIfAbsent("20161212", SimpleCacheExample::loadFromDatabase);
        System.out.println(orderCount1);
        System.out.println(orderCount2);
        cache.invalidate("20161212");
    }

    private static Integer loadFromDatabase(String key) {
        //...
        return 1000;
    }
}
