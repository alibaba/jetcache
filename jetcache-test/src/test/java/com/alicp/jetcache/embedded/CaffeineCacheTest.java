package com.alicp.jetcache.embedded;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import org.junit.Test;

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
    }

}
