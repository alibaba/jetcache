package com.alicp.jetcache.anno.inittestbeans;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import org.junit.jupiter.api.Assertions;

/**
 * Created on 2017/5/5.
 *
 * @author huangli
 */
public class CreateCacheInitTestBean {
    @CreateCache
    private Cache cache;

    public void doTest() {
        Assertions.assertNotNull(cache);
        Assertions.assertTrue(cache.PUT("K1", "V1").isSuccess());
        Assertions.assertEquals("V1", cache.get("K1"));

    }

}
