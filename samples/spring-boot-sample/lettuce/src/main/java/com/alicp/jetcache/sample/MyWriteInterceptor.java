package com.alicp.jetcache.sample;

import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义 Write Interceptor 示例
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
        // 在这里可以拦截并修改写操作
        // 例如：记录日志、修改值、修改过期时间等
        logger.info("Interceptor [{}] - Operation: {}, Key: {}, Value: {}, Expire: {} {}",
                name,
                ctx.getOp(),
                ctx.getKeyObj(),
                ctx.getValueObj(),
                ctx.getExpireAfterWrite(),
                ctx.getTimeUnit());

        // 示例：可以修改值（注意：这里只是示例，实际使用时需要谨慎）
        // ctx.setValueBytes(...);

        // 示例：可以修改过期时间（注意：这里只是示例，实际使用时需要谨慎）
        // 由于 WriteContext 的字段是 final 的，不能直接修改
        // 如需修改，需要在实际写入前拦截并替换
    }
}
