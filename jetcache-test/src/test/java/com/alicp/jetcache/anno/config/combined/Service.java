package com.alicp.jetcache.anno.config.combined;

import com.alicp.jetcache.anno.Cached;

/**
 * Created on 2017/2/14.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface Service {
    @Cached
    int combinedTest1();

    int combinedTest2();
}
