/**
 * Created on  13-09-24 10:20
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import org.junit.Test;

import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCacheTest extends AbstractEmbeddedCacheTest {

    @Override
    protected Function<CacheConfig, Cache> getBuildFunc() {
        return (c) -> new LinkedHashMapCache((EmbeddedCacheConfig) c);
    }

    @Override
    @Test
    public void test() throws Exception {
        super.test();
    }

    @Override
    @Test
    public void testConcurrent() throws Exception {
        super.testConcurrent();
    }
}
