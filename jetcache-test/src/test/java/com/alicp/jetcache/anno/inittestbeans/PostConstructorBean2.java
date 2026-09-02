package com.alicp.jetcache.anno.inittestbeans;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Created on 2017/5/4.
 *
 * @author huangli
 */
@Component
public class PostConstructorBean2 extends CreateCacheInitTestBean {

    @PostConstruct
    public void doTest() {
        super.doTest();
    }


}
