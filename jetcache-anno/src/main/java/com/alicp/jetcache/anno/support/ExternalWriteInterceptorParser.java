package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;

import java.util.List;

/**
 * Parses annotation-based external write interceptor configuration.
 */
public interface ExternalWriteInterceptorParser {
    List<ExternalCacheWriteInterceptor> parseExternalWriteInterceptors(String value);
}
