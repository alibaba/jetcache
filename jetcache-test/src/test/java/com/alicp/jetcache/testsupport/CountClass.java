/**
 * Created on  13-09-23 09:33
 */
package com.alicp.jetcache.testsupport;

import com.alicp.jetcache.test.support.DynamicQuery;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CountClass implements Count {
    private int count;

    @Override
    public int count() {
        return count++;
    }

    @Override
    public int count(int p) {
        return count++ + p;
    }

    @Override
    public int count(String s, int p) {
        return s.hashCode() + p + count++;
    }

    @Override
    public int count(DynamicQuery q, int p) {
        return q.hashCode() + p + count++;
    }

    private boolean first = true;

    /**
     * first invoke return null.
     */
    public Integer countNull() {
        if(first){
            first = false;
            return null;
        }
        return count++;
    }

    @Override
    public int update(String key, int newCount) {
        count = newCount;
        return newCount;
    }
}
