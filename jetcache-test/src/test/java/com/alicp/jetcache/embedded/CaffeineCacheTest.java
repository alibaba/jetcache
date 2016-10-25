package com.alicp.jetcache.embedded;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheResultCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CaffeineCacheTest extends AbstractEmbeddedCacheTest {
    @Override
    protected Function<CacheConfig, Cache> getBuildFunc() {
        return (c) -> new CaffeineCache((EmbeddedCacheConfig) c);
    }

    @Test
    public void test() throws Exception {
        super.test(false);

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
