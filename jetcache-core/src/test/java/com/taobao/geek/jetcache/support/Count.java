/**
 * Created on  13-09-23 17:37
 */
package com.taobao.geek.jetcache.support;

/**
 * @author yeli.hl
 */
public interface Count {
    public int count();

    public int count(int p);

    public int count(String s, int p);

    public int count(DynamicQuery q, int p);
}
