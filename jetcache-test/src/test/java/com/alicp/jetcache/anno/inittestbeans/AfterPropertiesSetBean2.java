package com.alicp.jetcache.anno.inittestbeans;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Created on 2017/5/5.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Component
public class AfterPropertiesSetBean2 extends CreateCacheInitTestBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        super.doTest();
    }
}
