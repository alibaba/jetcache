package com.alicp.jetcache.anno.method.interfaces;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;

public interface I8 {
    @Cached(name = "c1", key = "args[0]")
    int count(String id);

    @CacheUpdate(name = "c1", key = "#id", value = "args[1]")
    void update(String id, int value);

    @CacheUpdate(name = "c2", key = "args[0]", value = "args[1]")
    void update2(String id, int value);

    @CacheInvalidate(name = "c1", key = "#id")
    void delete(String id);

    @CacheInvalidate(name = "c2", key = "args[0]")
    void delete2(String id);

    @CacheUpdate(name = "c1", key = "#id", value="#result")
    int randomUpdate(String id);

    @CacheUpdate(name = "c1", key = "#id", value="result")
    int randomUpdate2(String id);
}
