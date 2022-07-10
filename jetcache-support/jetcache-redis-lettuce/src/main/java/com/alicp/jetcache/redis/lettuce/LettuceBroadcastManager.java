/**
 * Created on 2022/7/6.
 */
package com.alicp.jetcache.redis.lettuce;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.ResultData;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.support.JetCacheExecutor;
import com.alicp.jetcache.support.SquashedLogger;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class LettuceBroadcastManager extends RedisPubSubAdapter<byte[], byte[]> implements BroadcastManager {
    private static final Logger logger = LoggerFactory.getLogger(LettuceBroadcastManager.class);

    private final RedisLettuceCacheConfig<Object, Object> config;
    private final byte[] channel;
    private final RedisPubSubAsyncCommands<byte[], byte[]> asyncCommands;

    private volatile Consumer<CacheMessage> consumer;
    private boolean subscribeThreadStart;

    public LettuceBroadcastManager(RedisLettuceCacheConfig<Object, Object> config) {
        if (config.getBroadcastChannel() == null) {
            throw new CacheConfigException("BroadcastChannel not set");
        }
        if (config.getPubSubConnection() == null) {
            throw new CacheConfigException("PubSubConnection not set");
        }
        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }

        this.config = config;
        this.channel = config.getBroadcastChannel().getBytes(StandardCharsets.UTF_8);
        this.asyncCommands = config.getPubSubConnection().async();
    }

    @Override
    public CacheResult publish(CacheMessage cacheMessage) {
        try {
            byte[] value = config.getValueEncoder().apply(cacheMessage);
            RedisFuture<Long> future = asyncCommands.publish(channel, value);
            return new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() ->
                            SquashedLogger.getLogger(logger).error("jetcache publish error", ex));
                    return new ResultData(ex);
                } else {
                    return new ResultData(CacheResultCode.SUCCESS, null, null);
                }
            }));
        } catch (Exception ex) {
            SquashedLogger.getLogger(logger).error("jetcache publish error", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    public synchronized void startSubscribe(Consumer<CacheMessage> consumer) {
        if (subscribeThreadStart) {
            throw new IllegalStateException("startSubscribe has invoked");
        }
        this.consumer = consumer;

        config.getPubSubConnection().addListener(this);
        this.subscribeThreadStart = true;
    }

    @Override
    public void message(byte[] channel, byte[] message) {
        consumer.accept(convert(message, config.getValueDecoder()));
    }

    @Override
    public void close() {
        config.getPubSubConnection().removeListener(this);
        config.getPubSubConnection().close();
    }
}
