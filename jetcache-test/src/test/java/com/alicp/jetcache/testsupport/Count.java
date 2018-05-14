/**
 * Created on  13-09-23 17:37
 */
package com.alicp.jetcache.testsupport;

import com.alicp.jetcache.test.support.DynamicQuery;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface Count {
    int count();

    int count(int p);

    int count(String s, int p);

    int count(DynamicQuery q, int p);

    int update(String key, int newCount);
}
