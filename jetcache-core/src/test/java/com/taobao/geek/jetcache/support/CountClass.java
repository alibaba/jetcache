/**
 * Created on  13-09-23 09:33
 */
package com.taobao.geek.jetcache.support;

/**
 * @author yeli.hl
 */
public class CountClass {
    private static int count;

    public int count() {
        return count++;
    }

    public int count(int p) {
        return count++ + p;
    }

    public int count(String s, int p) {
        return s.hashCode() + p + count++;
    }

    public int count(DynamicQuery q, int p) {
        return q.hashCode() + p + count++;
    }

}
