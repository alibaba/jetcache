package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.method.interfaces.I6;

public class C6 implements I6 {
    int count;

    @EnableCache
    @Cached(enabled = false)
    public int count() {
        return count++;
    }

    public int countWithoutCache() {
        return count++;
    }
}