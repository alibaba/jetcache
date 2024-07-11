package com.alicp.jetcache.anno.method.interfaces;

import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;

import java.util.concurrent.TimeUnit;

public interface I9 {
    @Cached
    @CacheRefresh(refresh = 100, timeUnit = TimeUnit.MILLISECONDS)
    int count();

    @Cached(key = "#a", cacheType = CacheType.BOTH)
    @CacheRefresh(refresh = 100, timeUnit = TimeUnit.MILLISECONDS)
    int count(int a, int b);
}
