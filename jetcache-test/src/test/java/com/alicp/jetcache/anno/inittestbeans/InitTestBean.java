package com.alicp.jetcache.anno.inittestbeans;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.test.beans.TestBean;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created on 2017/5/5.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class InitTestBean {
    @CreateCache
    private Cache cache;

    @Autowired
    private TestBean testBean;

    public void doTest() {
        Assert.assertNotNull(cache);
        Assert.assertTrue(cache.PUT("K1", "V1").isSuccess());
        Assert.assertEquals("V1", cache.get("K1"));

        Assert.assertEquals(testBean.count(), testBean.count());
    }

}
