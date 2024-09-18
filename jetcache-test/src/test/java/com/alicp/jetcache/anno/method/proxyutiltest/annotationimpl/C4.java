package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I4_2;

public class C4 implements I4_2 {
    int count;

    public int count() {
        return count++;
    }

    public int countWithoutCache() {
        return count++;
    }
}