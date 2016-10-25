/**
 * Created on  13-09-19 21:49
 */
package com.alicp.jetcache.anno.spring.beans;

import com.alicp.jetcache.anno.Cached;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Service {

    int notCachedCount();

    int countWithAnnoOnClass();

    @Cached
    int countWithAnnoOnInterface();
}
