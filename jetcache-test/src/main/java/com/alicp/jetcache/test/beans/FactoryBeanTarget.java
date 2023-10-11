/**
 * Created on  13-10-28 23:43
 */
package com.alicp.jetcache.test.beans;

import com.alicp.jetcache.anno.Cached;

/**
 * @author huangli
 */
public interface FactoryBeanTarget {
    @Cached
    int count();
}
