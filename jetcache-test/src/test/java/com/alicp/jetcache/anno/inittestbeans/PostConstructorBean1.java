package com.alicp.jetcache.anno.inittestbeans;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 2017/5/4.
 *
 * @author huangli
 */
@Component
public class PostConstructorBean1 extends MethodCacheInitTestBean {

    @PostConstruct
    public void doTest() {
        super.doTest();
    }


}
