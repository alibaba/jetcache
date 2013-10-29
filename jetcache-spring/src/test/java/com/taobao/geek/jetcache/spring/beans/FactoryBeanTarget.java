/**
 * Created on  13-10-28 23:43
 */
package com.taobao.geek.jetcache.spring.beans;

import com.taobao.geek.jetcache.Cached;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface FactoryBeanTarget {
    @Cached
    public int count();
}
