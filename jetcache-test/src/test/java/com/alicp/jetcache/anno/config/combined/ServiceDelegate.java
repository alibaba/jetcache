package com.alicp.jetcache.anno.config.combined;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created on 2017/2/14.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
//@Component
public class ServiceDelegate implements Service {

    @Autowired
    private Service service;

    @Override
    public int m1() {
        return service.m1();
    }

    @Override
    public int m2() {
        return service.m2();
    }
}
