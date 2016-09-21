/**
 * Created on  13-09-19 21:49
 */
package com.alicp.jetcache.spring.beans;

import com.alicp.jetcache.anno.Cached;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Service {
    int count();

    @Cached
    int countWithAnnoOnInterface();
}
