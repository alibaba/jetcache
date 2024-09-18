package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I2;

public class C2 implements I2 {
    int count;

    public int count() {
        return count++;
    }

    public int countWithoutCache() {
        return count++;
    }
}