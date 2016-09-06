/**
 * Created on  13-10-28 23:43
 */
package com.alicp.jetcache.spring.beans;

import com.alicp.jetcache.Cached;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface FactoryBeanTarget {
    @Cached
    public int count();
}
