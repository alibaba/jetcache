/**
 * Created on  13-10-17 22:34
 */
package com.alicp.jetcache.embedded;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface InnerMap {
    Object getValue(Object key);

    void putValue(Object key, Object value);

    boolean removeValue(Object key);

    boolean putIfAbsentValue(Object key, Object value);
}
