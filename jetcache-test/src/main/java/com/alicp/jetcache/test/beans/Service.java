/**
 * Created on  13-09-19 21:49
 */
package com.alicp.jetcache.test.beans;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface Service {

    int notCachedCount();

    int countWithAnnoOnClass();

    @Cached
    int countWithAnnoOnInterface();

    @EnableCache
    int enableCacheWithAnnoOnInterface(TestBean bean);

    int enableCacheWithAnnoOnClass(TestBean bean);

    int enableCacheWithNoCacheCount(TestBean bean);

    @Cached(name = "c1", key = "args[0]")
    int count(String id);

    @CacheUpdate(name = "c1", key = "#id", value = "args[1]")
    void update(String id, int value);

    @CacheUpdate(name = "c2", key = "args[0]", value = "args[1]")
    void update2(String id, int value);

    @CacheInvalidate(name = "c1", key = "args[0]")
    void delete(String id);

    @CacheInvalidate(name = "c2", key = "args[0]")
    void delete2(String id);
}
