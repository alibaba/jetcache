/**
 * Created on  13-10-28 23:44
 */
package com.alicp.jetcache.test.beans;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class FactoryBeanTargetImpl implements FactoryBeanTarget {

    int count;

    @Override
    public int count() {
        return count++;
    }
}
