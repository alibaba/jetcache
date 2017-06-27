/**
 * Created on  13-10-03 00:03
 */
package com.alicp.jetcache;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheConfigException extends CacheException {
    private static final long serialVersionUID = -3401839239922905427L;

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
