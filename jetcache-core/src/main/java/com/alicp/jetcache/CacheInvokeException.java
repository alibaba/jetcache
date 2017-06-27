package com.alicp.jetcache;

/**
 * Created on 2016/10/13.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheInvokeException extends CacheException {

    private static final long serialVersionUID = -9002505061387176702L;

    private Throwable target;

    public CacheInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheInvokeException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable getCause(){
        return target;
    }

}
