/**
 * Created on  13-10-28 23:44
 */
package com.alicp.jetcache.anno.springtest.beans;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class FactoryBeanTargetImpl implements FactoryBeanTarget {

    int count;

    public int count() {
        return count++;
    }
}
