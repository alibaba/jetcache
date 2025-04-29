package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I11;

public class C11 implements I11 {
    int count;

    @Override
    public int count(int id) {
        if (id == 0) {
            throw new IllegalArgumentException("ID cannot be zero");
        }
        return count++;
    }

    @Override
    public int count2(int id) {
        if (id == 0) {
            throw new RuntimeException("ID cannot be zero");
        }
        return count++;
    }

    @Override
    public void delete(String id) {
        // Do nothing for this test
    }
}
