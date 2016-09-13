/**
 * Created on  13-10-17 22:34
 */
package com.alicp.jetcache.local;

import java.lang.ref.SoftReference;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface AreaCache {
    public Object getValue(Object key);

    public Object putValue(Object key, Object value);

    public Object removeValue(Object key);
}
