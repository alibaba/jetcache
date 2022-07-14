package com.alicp.jetcache.redis;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.support.SquashedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.UnifiedJedis;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Created on 2022-05-03
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisBroadcastManager implements BroadcastManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisBroadcastManager.class);

    private final byte[] channel;
    private final String channelStr;
    private final RedisCacheConfig<Object, Object> config;

    private volatile Consumer<CacheMessage> consumer;
    private volatile CacheMessagePubSub cacheMessagePubSub;
    private volatile boolean closed;
    private volatile boolean subscribe;
    private boolean subscribeThreadStart;

    public RedisBroadcastManager(RedisCacheConfig<Object, Object> config) {
        this.channelStr = config.getBroadcastChannel();
        if (this.channelStr == null) {
            throw new CacheConfigException("broadcastChannel not set");
        }
        this.channel = channelStr.getBytes(StandardCharsets.UTF_8);
        this.config = config;

        if (config.getJedis() == null && config.getJedisPool() == null) {
            throw new CacheConfigException("no jedis");
        }
        if (config.getJedis() != null && config.getJedisPool() != null) {
            throw new CacheConfigException("'jedis' and 'jedisPool' can't set simultaneously");
        }

        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }
    }

    @Override
    public synchronized void startSubscribe(Consumer<CacheMessage> consumer) {
        if (subscribeThreadStart) {
            throw new IllegalStateException("subscribe thread is started");
        }
        this.consumer = consumer;
        this.cacheMessagePubSub = new CacheMessagePubSub();
        Thread subThread;
        subThread = new Thread(this::runSubThread, "Sub_" + channelStr);
        subThread.setDaemon(true);
        subThread.start();
        this.subscribeThreadStart = true;
    }

    private void runSubThread() {
        while (!closed) {
            runSubThread0();
        }
    }

    private void runSubThread0() {
        Object jedisObj = null;
        try {
            jedisObj = writeCommands();
            if (jedisObj instanceof Jedis) {
                subscribe = true;
                ((Jedis) jedisObj).subscribe(cacheMessagePubSub, channel);
            } else if (jedisObj instanceof UnifiedJedis) {
                subscribe = true;
                ((UnifiedJedis) jedisObj).subscribe(cacheMessagePubSub, channel);
            }
        } catch (Throwable e) {
            SquashedLogger.getLogger(logger).error("run jedis subscribe thread error: {}", e);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // ignore
            }
        } finally {
            subscribe = false;
            RedisCache.closeJedis(jedisObj);
        }
    }

    Object writeCommands() {
        return config.getJedis() != null ? config.getJedis() : config.getJedisPool().getResource();
    }

    @Override
    public CacheResult publish(CacheMessage message) {
        Object jedisObj = null;
        try {
            jedisObj = writeCommands();
            byte[] value = config.getValueEncoder().apply(message);
            if (jedisObj instanceof Jedis) {
                ((Jedis) jedisObj).publish(channel, value);
            } else {
                ((UnifiedJedis) jedisObj).publish(channel, value);
            }
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            SquashedLogger.getLogger(logger).error("jetcache publish error", ex);
            return new CacheResult(ex);
        } finally {
            RedisCache.closeJedis(jedisObj);
        }
    }


    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (subscribe) {
            try {
                this.cacheMessagePubSub.unsubscribe(channel);
            } catch (Exception e) {
                logger.warn("unsubscribe {} fail", channelStr, e);
            }
        }
    }

    class CacheMessagePubSub extends BinaryJedisPubSub {

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            consumer.accept(convert(message, config.getValueDecoder()));
        }
    }
}
