package com.alicp.jetcache;

/**
 * Created on 2016/12/20.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface AutoReleaseLock extends AutoCloseable {
    /**
     * Release the lock use Java 7 try-with-resources.
     */
    @Override
    void close();
}
