/**
 * Created on  13-09-23 11:31
 */
package com.alicp.jetcache.anno.context;

import com.alicp.jetcache.CacheException;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Deprecated
public class CallbackException extends CacheException {
    public CallbackException(Throwable cause) {
        super(cause);
    }
}
