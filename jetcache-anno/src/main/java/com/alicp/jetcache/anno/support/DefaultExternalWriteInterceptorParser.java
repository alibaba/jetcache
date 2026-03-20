package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;

import java.util.List;

/**
 * Default parser for non-Spring environments.
 */
public class DefaultExternalWriteInterceptorParser implements ExternalWriteInterceptorParser {

    @Override
    public List<ExternalCacheWriteInterceptor> parseExternalWriteInterceptors(String value) {
        throw new CacheConfigException("externalWriteInterceptors is configured, " +
                "but no ExternalWriteInterceptorParser is available for the current ConfigProvider");
    }
}
