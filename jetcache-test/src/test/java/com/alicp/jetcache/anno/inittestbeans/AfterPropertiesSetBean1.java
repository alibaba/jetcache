package com.alicp.jetcache.anno.inittestbeans;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Created on 2017/5/5.
 *
 * @author huangli
 */
@Component
public class AfterPropertiesSetBean1 extends MethodCacheInitTestBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        super.doTest();
    }
}
