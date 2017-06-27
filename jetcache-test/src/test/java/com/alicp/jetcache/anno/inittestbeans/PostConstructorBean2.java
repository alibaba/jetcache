package com.alicp.jetcache.anno.inittestbeans;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 2017/5/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Component
public class PostConstructorBean2 extends CreateCacheInitTestBean {

    @PostConstruct
    public void doTest() {
        super.doTest();
    }


}
