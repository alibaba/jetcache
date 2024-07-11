package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I9;

public  class C9 implements I9 {
    int count1;
    int count2;

    public int count() {
        return count1++;
    }

    @Override
    public int count(int a, int b) {
        return a + b + count2++;
    }
}
