package com.alicp.jetcache.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author youjie_li
 */
public class LoggingExternalCacheWriteInterceptor implements ExternalCacheWriteInterceptor{
    private static Logger logger = LoggerFactory.getLogger(LoggingExternalCacheWriteInterceptor.class);
    @Override
    public void intercept(WriteContext ctx) {
        logger.info("external cache writing. key: {} value length: {}",ctx.getKeyObj(),ctx.getValueBytes().length);
    }
}
