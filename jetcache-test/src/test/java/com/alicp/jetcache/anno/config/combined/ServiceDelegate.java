package com.alicp.jetcache.anno.config.combined;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created on 2017/2/14.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
//@Component
public class ServiceDelegate implements Service {

    @Autowired
    private Service service;

    @Override
    public int combinedTest1() {
        return service.combinedTest1();
    }

    @Override
    public int combinedTest2() {
        return service.combinedTest2();
    }
}
