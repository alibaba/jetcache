package com.alicp.jetcache.anno.method.interfaces;

import com.alicp.jetcache.anno.Cached;

import java.util.concurrent.TimeUnit;

public interface I12 {
    @Cached(expire = 100, timeUnit = TimeUnit.MILLISECONDS)
    int count(int p);
}
