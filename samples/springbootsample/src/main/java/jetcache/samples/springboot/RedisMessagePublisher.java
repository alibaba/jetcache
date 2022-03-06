package jetcache.samples.springboot;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.support.CacheMessagePublisher;
import com.alicp.jetcache.redis.RedisCacheConfig;
import com.alicp.jetcache.anno.support.ConfigProvider;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Component
public class RedisMessagePublisher implements CacheMessagePublisher {

    @Value("${jetcache.cacheMessagePublisher.topic}")
    String topicName;

    @Autowired
    private ConfigProvider configProvider;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisMessageListener redisMessageListener;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Override
    public void publish(String area, String cacheName, CacheMessage cacheMessage) {
        int type = cacheMessage.getType();

        if (type == CacheMessage.TYPE_PUT || type == CacheMessage.TYPE_PUT_ALL) {
            return;
        }

        Cache cache = configProvider.getCacheManager().getCache(area, cacheName);

        if (cache.config() instanceof RedisCacheConfig) {
            return;
        }

        CacheMessageWithName cacheMessageWithName = new CacheMessageWithName();

        cacheMessageWithName.setArea(area);
        cacheMessageWithName.setCacheName(cacheName);
        cacheMessageWithName.setCacheMessage(cacheMessage);

        redisTemplate.convertAndSend(topicName, cacheMessageWithName);
    }

    @PostConstruct
    public void initConsumer() {
        redisMessageListenerContainer.addMessageListener(redisMessageListener, new ChannelTopic(topicName));
    }

}
