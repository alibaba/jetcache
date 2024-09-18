package com.alicp.jetcache.anno.method.interfaces;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.Cached;

public interface I11 {
    @Cached
    int count(int id);

    @Cached
    int count2(int id);

    @CacheInvalidate(name = "c1", key = "args[0]")
    void delete(String id);
}
