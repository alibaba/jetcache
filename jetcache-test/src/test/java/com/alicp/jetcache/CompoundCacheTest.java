package com.alicp.jetcache;

import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCache;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
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
        Cache<Object, Object> l1Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(10)
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        Cache<Object, Object> l2Cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(100000)
                .expireAfterWrite(200, TimeUnit.MILLISECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .softValues()
                .buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig) c))
                .build();
        cache = new CompoundCache<>(l1Cache, l2Cache);
        baseTest();
    }
}
