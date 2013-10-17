/**
 * Created on  13-10-08 11:00
 */
package com.taobao.geek.jetcache.local;

import java.lang.ref.SoftReference;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class CacheObject {
    Object value;
    long expireTime;
}
