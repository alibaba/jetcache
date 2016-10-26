package com.alicp.jetcache;

import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CompoundCacheTest extends AbstractCacheTest {

    @Test
    public void test() throws Exception {
        WrapValueCache<Object, Object> l1Cache = (WrapValueCache<Object, Object>) EmbeddedCacheBuilder
                .createEmbeddedCacheBuilder()
                .limit(10)
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        WrapValueCache<Object, Object> l2Cache = (WrapValueCache<Object, Object>) EmbeddedCacheBuilder
                .createEmbeddedCacheBuilder()
                .limit(100000)
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .softValues()
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        cache = new CompoundCache<>(l1Cache, l2Cache);
        baseTest();
        expireAfterWriteTest(200);


        cache.put("K1", "V1");
        Thread.sleep(50);
        l1Cache.invalidate("K1");
        Assert.assertEquals("V1", cache.get("K1"));
        CacheGetResult<CacheValueHolder<Object>> h1 = l1Cache.GET_HOLDER("K1");
        CacheGetResult<CacheValueHolder<Object>> h2 = l2Cache.GET_HOLDER("K1");
        Assert.assertEquals(h1.getValue().getExpireTime(), h2.getValue().getExpireTime(), 5);
    }
}
