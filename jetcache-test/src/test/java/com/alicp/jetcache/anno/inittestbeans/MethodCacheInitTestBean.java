package com.alicp.jetcache.anno.inittestbeans;

import com.alicp.jetcache.test.beans.TestBean;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals(testBean.count(), testBean.count());
    }

}
