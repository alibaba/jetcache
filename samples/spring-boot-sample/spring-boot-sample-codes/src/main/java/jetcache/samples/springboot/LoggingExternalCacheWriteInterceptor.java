package jetcache.samples.springboot;

import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author youjie_li
 */
@Component("loggingExternalCacheWriteInterceptor")
public class LoggingExternalCacheWriteInterceptor implements ExternalCacheWriteInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingExternalCacheWriteInterceptor.class);
    @Override
    public WriteInterceptDecision intercept(WriteContext ctx) {
        logger.info("external cache writing. key: {} value length: {}", ctx.getKeyObj(), ctx.getValueSize());
        return WriteInterceptDecision.allow();
    }
}
