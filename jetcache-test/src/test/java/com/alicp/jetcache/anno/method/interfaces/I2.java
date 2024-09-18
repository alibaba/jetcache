package com.alicp.jetcache.anno.method.interfaces;


import com.alicp.jetcache.anno.Cached;

public interface I2 {
    @Cached
    int count();

    int countWithoutCache();
}
