package com.alicp.jetcache.anno.method.interfaces;

import com.alicp.jetcache.anno.CachePenetrationProtect;
import com.alicp.jetcache.anno.Cached;

public interface I10 {
    @Cached
    int count1(int p);

    @Cached
    @CachePenetrationProtect
    int count2(int p);
}