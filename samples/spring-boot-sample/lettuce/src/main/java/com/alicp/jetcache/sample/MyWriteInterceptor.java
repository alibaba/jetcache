package com.alicp.jetcache.sample;

import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义写入前回调示例。
 *
 * @author jetcache
 */
public class MyWriteInterceptor implements ExternalCacheWriteInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MyWriteInterceptor.class);

    private final String name;

    public MyWriteInterceptor() {
        this("default");
    }

    public MyWriteInterceptor(String name) {
        this.name = name;
    }

    @Override
    public <K, V> void intercept(WriteContext<K, V> ctx) {
        logger.info("Interceptor [{}] - Operation: {}, Key: {}, Value: {}, Expire: {} {}",
                name,
                ctx.getOp(),
                ctx.getKeyObj(),
                ctx.getValueObj(),
                ctx.getExpireAfterWrite(),
                ctx.getTimeUnit());
    }
}
