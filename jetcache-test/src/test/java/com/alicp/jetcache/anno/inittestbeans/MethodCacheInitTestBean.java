package com.alicp.jetcache.anno.inittestbeans;

import com.alicp.jetcache.test.beans.TestBean;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created on 2017/5/5.
 *
 * @author huangli
 */
public class MethodCacheInitTestBean {

    @Autowired
    private TestBean testBean;

    public void doTest() {
        Assert.assertEquals(testBean.count(), testBean.count());
    }

}
