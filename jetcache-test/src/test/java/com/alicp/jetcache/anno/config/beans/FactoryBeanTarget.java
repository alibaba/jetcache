/**
 * Created on  13-10-28 23:43
 */
package com.alicp.jetcache.anno.config.beans;

import com.alicp.jetcache.anno.Cached;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface FactoryBeanTarget {
    @Cached
    int count();
}
