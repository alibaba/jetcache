package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.method.interfaces.I5;

public class C5 implements I5 {
    int count;

    @Cached(enabled = false)
    public int count() {
        return count++;
    }

    public int countWithoutCache() {
        return count++;
    }
}
