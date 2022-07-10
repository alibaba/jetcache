/**
 * Created on 2019/6/10.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface BroadcastManager extends AutoCloseable {
    Logger logger = LoggerFactory.getLogger(BroadcastManager.class);

    CacheResult publish(CacheMessage cacheMessage);

    void startSubscribe(Consumer<CacheMessage> consumer);

    @Override
    default void close() throws Exception {
    }

    default CacheMessage convert(byte[] message, Function<byte[], Object> decoder) {
        try {
            if (message == null) {
                return null;
            }
            Object value = decoder.apply(message);
            if (value instanceof CacheMessage) {
                return (CacheMessage) value;
            } else {
                logger.error("the message is not instance of CacheMessage, class={}", value.getClass());
                return null;
            }
        } catch (Throwable e) {
            SquashedLogger.getLogger(logger).error("receive cache notify error", e);
            return null;
        }
    }
}
