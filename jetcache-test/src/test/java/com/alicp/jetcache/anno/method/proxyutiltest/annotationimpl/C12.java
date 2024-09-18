package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I12;

public class C12 implements I12 {
    int count;

    @Override
    public int count(int p) {
        return count++;
    }
}