package com.alicp.jetcache.anno.inittestbeans;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import org.junit.Assert;

/**
 * Created on 2017/5/5.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CreateCacheInitTestBean {
    @CreateCache
    private Cache cache;

    public void doTest() {
        Assert.assertNotNull(cache);
        Assert.assertTrue(cache.PUT("K1", "V1").isSuccess());
        Assert.assertEquals("V1", cache.get("K1"));

    }

}
