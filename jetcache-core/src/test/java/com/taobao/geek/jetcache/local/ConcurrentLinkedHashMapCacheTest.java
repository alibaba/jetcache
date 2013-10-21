/**
 * Created on  13-09-24 10:20
 */
package com.taobao.geek.jetcache.local;

import com.taobao.geek.jetcache.support.CacheConfig;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ConcurrentLinkedHashMapCacheTest extends AbstractLocalCacheTest {
    @Override
    protected void setup(boolean useSofeRef) {
        cache = new ConcurrentLinkedHashMapCache(useSofeRef);
        cc = new CacheConfig();
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
    public void testLRU3() throws Exception {
        super.testLRU3();
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
