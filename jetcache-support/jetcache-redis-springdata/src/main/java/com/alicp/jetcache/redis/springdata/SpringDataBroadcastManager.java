/**
 * Created on 2022/07/15.
 */
package com.alicp.jetcache.redis.springdata;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.support.BroadcastManager;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.support.SquashedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringDataBroadcastManager extends BroadcastManager {

    private static final Logger logger = LoggerFactory.getLogger(SpringDataBroadcastManager.class);

    private final RedisSpringDataCacheConfig config;
    private final MessageListener listener = this::onMessage;
    private final byte[] channel;
    private volatile RedisMessageListenerContainer listenerContainer;

    public SpringDataBroadcastManager(CacheManager cacheManager, RedisSpringDataCacheConfig config) {
        super(cacheManager);
        this.config = config;
        checkConfig(config);
        if (config.getConnectionFactory() == null) {
            throw new CacheConfigException("connectionFactory is required");
        }
        this.channel = config.getBroadcastChannel().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public CacheResult publish(CacheMessage cacheMessage) {
        RedisConnection con = null;
        try {
            con = config.getConnectionFactory().getConnection();
            byte[] body = (byte[]) config.getValueEncoder().apply(cacheMessage);
            con.publish(channel, body);
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            SquashedLogger.getLogger(logger).error("jetcache publish error", ex);
            return new CacheResult(ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    SquashedLogger.getLogger(logger).error("RedisConnection close fail", e);
                }
            }
        }
    }

    @Override
    public synchronized void startSubscribe() {
        if (this.listenerContainer != null) {
            throw new IllegalStateException("subscribe thread is started");
        }
        Topic topic = new ChannelTopic(config.getBroadcastChannel());
        if (config.getListenerContainer() == null) {
            RedisMessageListenerContainer c = new RedisMessageListenerContainer();
            c.setConnectionFactory(config.getConnectionFactory());
            c.afterPropertiesSet();
            c.start();
            this.listenerContainer = c;
            logger.info("create RedisMessageListenerContainer instance");
        } else {
            this.listenerContainer = config.getListenerContainer();
        }
        this.listenerContainer.addMessageListener(listener, topic);
        logger.info("subscribe jetcache invalidate notification. channel={}", config.getBroadcastChannel());
    }

    private void onMessage(Message message, byte[] pattern) {
        processNotification(message.getBody(), config.getValueDecoder());
    }

    @Override
    public synchronized void close() throws Exception {
        if (this.listenerContainer != null) {
            this.listenerContainer.removeMessageListener(listener);
            if (this.config.getListenerContainer() == null) {
                this.listenerContainer.destroy();
            }
        }
        this.listenerContainer = null;
    }
}
