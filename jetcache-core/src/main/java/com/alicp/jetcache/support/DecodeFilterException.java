package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheException;

/**
 * Exception thrown when a class is blocked by the deserialization filter.
 *
 * @author huangli
 */
public class DecodeFilterException extends CacheException {

    private static final long serialVersionUID = 1L;

    public DecodeFilterException(String className) {
        super("Class '" + className + "' is not allowed by the deserialization filter");
    }
}
