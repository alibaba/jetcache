/**
 * Created on  13-09-19 21:49
 */
package com.alicp.jetcache.test.beans;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
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
}
