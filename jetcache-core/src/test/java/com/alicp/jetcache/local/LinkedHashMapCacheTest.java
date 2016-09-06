/**
 * Created on  13-09-24 10:20
 */
package com.alicp.jetcache.local;

import com.alicp.jetcache.support.Cache;
import com.alicp.jetcache.support.CacheConfig;
import com.alicp.jetcache.support.CacheResult;
import com.alicp.jetcache.support.CacheResultCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCacheTest extends AbstractLocalCacheTest {

    @Override
    protected void setup(boolean useSofeRef) {
        cache = new LinkedHashMapCache(useSofeRef);
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
