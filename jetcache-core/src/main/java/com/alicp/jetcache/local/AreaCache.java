/**
 * Created on  13-10-17 22:34
 */
package com.alicp.jetcache.local;

import java.lang.ref.SoftReference;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface AreaCache {
    public Object getValue(String key);

    public Object putValue(String key, Object value);

    public Object removeValue(String key);
}
