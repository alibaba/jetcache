package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.method.interfaces.I7_1;
import com.alicp.jetcache.anno.method.interfaces.I7_2;

public class C7_2 implements I7_2 {
    public I7_1 service;

    @EnableCache
    public int count() {
        return service.count();
    }

    @EnableCache
    public int countWithoutCache() {
        return service.countWithoutCache();
    }
}
