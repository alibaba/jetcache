/**
 * Created on  13-09-24 10:20
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.cache.CacheBuilderConfig;
import org.junit.Test;

import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ConcurrentLinkedHashMapCacheTest extends AbstractLocalCacheTest {

    @Override
    protected Function<CacheBuilderConfig, Cache> getBuildFunc() {
        return (c) -> new ConcurrentLinkedHashMapCache((LocalCacheConfig) c);
    }

    @Override
    @Test
    public void test1() throws Exception {
        super.test1();
    }

    @Override
    @Test
    public void testLRU1() {
        super.testLRU1();
    }

    @Override
    @Test
    public void testLRU2() {
        super.testLRU2();
    }

    @Override
    @Test
    public void testExpire() throws Exception {
        super.testExpire();
    }

    @Override
    @Test
    public void testNull() {
        super.testNull();
    }

    @Override
    @Test
    public void testConcurrent() throws Exception {
        super.testConcurrent();
    }
}
