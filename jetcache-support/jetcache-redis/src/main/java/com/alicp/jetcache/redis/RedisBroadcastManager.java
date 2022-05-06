package com.alicp.jetcache.redis;

import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
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
public class RedisBroadcastManager extends RedisCache<Object, Object> implements BroadcastManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisBroadcastManager.class);

    private final byte[] channel;
    private final String channelStr;

    private volatile Consumer<CacheMessage> consumer;
    private volatile CacheMessageConsumer cacheMessageConsumer;
    private volatile boolean closed;
    private volatile boolean subscribe;
    private boolean subscribeThreadStart;

    public RedisBroadcastManager(String channel, RedisCacheConfig<Object, Object> cacheConfig) {
        super(cacheConfig);
        this.channelStr = channel;
        this.channel = channel.getBytes(StandardCharsets.UTF_8);

    }

    @Override
    public synchronized void startSubscribe(Consumer<CacheMessage> consumer) {
        if (subscribeThreadStart) {
            throw new IllegalStateException("subscribe thread is started");
        }
        this.consumer = consumer;
        this.cacheMessageConsumer = new CacheMessageConsumer();
        Thread subThread;
        subThread = new Thread(this::runSubThread, "Sub_" + channel);
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
                ((Jedis) jedisObj).subscribe(cacheMessageConsumer, channel);
            } else if (jedisObj instanceof UnifiedJedis) {
                subscribe = true;
                ((UnifiedJedis) jedisObj).subscribe(cacheMessageConsumer, channel);
            }
        } catch (Throwable e) {
            logger.error("run jedis subscribe thread error: {}", e.toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // ignore
            }
        } finally {
            subscribe = false;
            closeJedis(jedisObj);
        }
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
            logError("PUBLISH", "N/A", ex);
            return new CacheResult(ex);
        } finally {
            closeJedis(jedisObj);
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
                this.cacheMessageConsumer.unsubscribe(channel);
            } catch (Exception e) {
                logger.warn("unsubscribe {} fail", channelStr, e);
            }
        }
        super.close();
    }

    class CacheMessageConsumer extends BinaryJedisPubSub {

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            try {
                if (message == null) {
                    return;
                }
                Object value = config.getValueDecoder().apply(message);
                if (value instanceof CacheMessage) {
                    consumer.accept((CacheMessage) value);
                } else {
                    logger.error("{} the message is not instance of CacheMessage, class={}", channelStr, value.getClass());
                }
            } catch (Throwable e) {
                logger.error("receive cache notify error", e);
            }
        }
    }
}
