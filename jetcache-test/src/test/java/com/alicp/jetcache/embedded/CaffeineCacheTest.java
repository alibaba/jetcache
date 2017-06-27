package com.alicp.jetcache.embedded;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import org.junit.Test;

import java.util.function.Function;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CaffeineCacheTest extends AbstractEmbeddedCacheTest {
    @Override
    protected Function<CacheConfig, Cache> getBuildFunc() {
        return (c) -> new CaffeineCache((EmbeddedCacheConfig) c);
    }

    @Test
    public void test() throws Exception {
        super.test(500, false);

//        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
//                .buildFunc(getBuildFunc()).expireAfterWrite(100, TimeUnit.MILLISECONDS).limit(2).build();
//        cache.put("K1", "V1");
//        cache.put("K2", "V2");
//        cache.put("K3", "V3");
//        System.out.println(cache.get("K1"));
//        System.out.println(cache.get("K2"));
//        System.out.println(cache.get("K3"));
//        System.out.println(cache.get("K1"));
//        System.out.println(cache.get("K2"));
//        System.out.println(cache.get("K3"));
    }

}
