package jetcache.samples.springboot;

import java.util.UUID;
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

    public static String processId = UUID.randomUUID().toString();

    @Value("${jetcache.cacheMessagePublisher.topic}")
    private String topicName;

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
        Cache cache = configProvider.getCacheManager().getCache(area, cacheName);

        if (cache.config() instanceof RedisCacheConfig) {
            return;
        }

        CacheMessageWithName cacheMessageWithName = new CacheMessageWithName();

        cacheMessageWithName.setArea(area);
        cacheMessageWithName.setCacheName(cacheName);
        cacheMessageWithName.setProcessId(processId);
        cacheMessageWithName.setType(cacheMessage.getType());
        cacheMessageWithName.setKeys(cacheMessage.getKeys());

        redisTemplate.convertAndSend(topicName, cacheMessageWithName);
    }

    @PostConstruct
    public void initConsumer() {
        redisMessageListenerContainer.addMessageListener(redisMessageListener, new ChannelTopic(topicName));
    }

}
