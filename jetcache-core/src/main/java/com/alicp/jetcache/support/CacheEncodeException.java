package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheException;

/**
 * Created on 2016/12/23.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheEncodeException extends CacheException {

    private static final long serialVersionUID = -1768444197009616269L;

    public CacheEncodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
