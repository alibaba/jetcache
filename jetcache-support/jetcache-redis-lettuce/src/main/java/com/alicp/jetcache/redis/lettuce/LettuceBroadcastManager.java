/**
 * Created on 2022/7/6.
 */
package com.alicp.jetcache.redis.lettuce;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.ResultData;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.support.JetCacheExecutor;
import com.alicp.jetcache.support.SquashedLogger;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huangli
 */
public class LettuceBroadcastManager extends BroadcastManager {
    private static final Logger logger = LoggerFactory.getLogger(LettuceBroadcastManager.class);

    private final RedisLettuceCacheConfig<Object, Object> config;
    private final byte[] channel;

    private volatile boolean subscribeThreadStart;
    private volatile RedisPubSubAdapter<byte[], byte[]> pubSubAdapter;

    private final LettuceConnectionManager lettuceConnectionManager;
    private final BaseRedisAsyncCommands<byte[], byte[]> stringAsyncCommands;

    private final ReentrantLock reentrantLock = new ReentrantLock();


    public LettuceBroadcastManager(CacheManager cacheManager, RedisLettuceCacheConfig<Object, Object> config) {
        super(cacheManager);
        checkConfig(config);
        if (config.getPubSubConnection() == null) {
            throw new CacheConfigException("PubSubConnection not set");
        }

        this.config = config;
        this.channel = config.getBroadcastChannel().getBytes(StandardCharsets.UTF_8);
        this.lettuceConnectionManager = config.getConnectionManager();
        this.lettuceConnectionManager.init(config.getRedisClient(), config.getConnection());
        this.stringAsyncCommands = (BaseRedisAsyncCommands<byte[], byte[]>) lettuceConnectionManager
                .asyncCommands(config.getRedisClient());
    }

    @Override
    public CacheResult publish(CacheMessage cacheMessage) {
        try {
            byte[] value = config.getValueEncoder().apply(cacheMessage);
            RedisFuture<Long> future = stringAsyncCommands.publish(channel, value);
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
    public void startSubscribe() {
        reentrantLock.lock();
        try {
            if (subscribeThreadStart) {
                throw new IllegalStateException("startSubscribe has invoked");
            }
            this.pubSubAdapter = new RedisPubSubAdapter<byte[], byte[]>() {
                @Override
                public void message(byte[] channel, byte[] message) {
                    processNotification(message, config.getValueDecoder());
                }
            };
            config.getPubSubConnection().addListener(this.pubSubAdapter);
            RedisPubSubAsyncCommands<byte[], byte[]> asyncCommands = config.getPubSubConnection().async();
            asyncCommands.subscribe(channel);
            this.subscribeThreadStart = true;
        }finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void close() {
        config.getPubSubConnection().removeListener(this.pubSubAdapter);
        config.getPubSubConnection().close();
    }
}
