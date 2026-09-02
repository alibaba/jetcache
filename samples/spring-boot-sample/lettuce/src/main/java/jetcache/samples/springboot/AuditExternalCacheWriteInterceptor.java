package jetcache.samples.springboot;

import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author youjie_li
 */
public class AuditExternalCacheWriteInterceptor implements ExternalCacheWriteInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuditExternalCacheWriteInterceptor.class);

    @Override
    public <K, V> WriteInterceptDecision intercept(WriteContext<K, V> ctx) {
        logger.info("Operation: {}, Key: {}, Value: {}, ValueSize: {}, Expire: {} {}",
                ctx.getOp(),
                ctx.getKeyObj(),
                ctx.getValueObj(),
                ctx.getValueSize(),
                ctx.getExpireAfterWrite(),
                ctx.getTimeUnit());
        return WriteInterceptDecision.allow();
    }
}
