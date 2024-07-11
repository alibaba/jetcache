package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.method.interfaces.I1;

public class C1 implements I1 {
    int count;

    @Cached
    public int count() {
        return count++;
    }

    public int countWithoutCache() {
        return count++;
    }
}
