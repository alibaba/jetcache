package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I3_2;

public class C3 implements I3_2 {
    int count;

    public int count() {
        return count++;
    }

    public int countWithoutCache() {
        return count++;
    }
}
