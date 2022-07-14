/**
 * Created on 2019/6/10.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheUtil;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.AbstractEmbeddedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * @author <a href="mailto:areyouok@gmail.com">huangli</a>
     */
    class CacheMessageConsumer implements Consumer<CacheMessage> {
        private final String sourceId;
        private final CacheManager cacheManager;

        public CacheMessageConsumer(String sourceId, CacheManager cacheManager) {
            this.sourceId = sourceId;
            this.cacheManager = cacheManager;
        }

        @Override
        public void accept(CacheMessage cacheMessage) {
            if (cacheMessage == null) {
                return;
            }
            if (sourceId.equals(cacheMessage.getSourceId())) {
                return;
            }
            Cache cache = cacheManager.getCache(cacheMessage.getArea(), cacheMessage.getCacheName());
            Cache absCache = CacheUtil.getAbstractCache(cache);
            if (!(absCache instanceof MultiLevelCache)) {
                return;
            }
            Cache[] caches = ((MultiLevelCache) absCache).caches();
            Set<Object> keys = Stream.of(cacheMessage.getKeys()).collect(Collectors.toSet());
            for (Cache c : caches) {
                Cache localCache = CacheUtil.getAbstractCache(c);
                if (localCache instanceof AbstractEmbeddedCache) {
                    localCache.REMOVE_ALL(keys);
                } else {
                    break;
                }
            }
        }
    }
}
