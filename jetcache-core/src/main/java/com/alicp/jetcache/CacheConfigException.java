/**
 * Created on  13-10-03 00:03
 */
package com.alicp.jetcache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfigException extends CacheException {
    public CacheConfigException(Throwable cause) {
        super(cause);
    }

    public CacheConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheConfigException(String message) {
        super(message);
    }
}
