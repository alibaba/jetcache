/**
 * Created on  13-09-23 17:37
 */
package com.alicp.jetcache.testsupport;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface Count {
    int count();

    int count(int p);

    int count(String s, int p);

    int count(DynamicQuery q, int p);
}
