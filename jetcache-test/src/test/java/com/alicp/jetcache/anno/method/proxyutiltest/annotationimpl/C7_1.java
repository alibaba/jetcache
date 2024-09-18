package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.method.interfaces.I7_1;

public class C7_1 implements I7_1 {
    int count;

    @Cached(enabled = false)
    public int count() {
        return count++;
    }

    @Override
    public int countWithoutCache() {
        return count++;
    }
}
