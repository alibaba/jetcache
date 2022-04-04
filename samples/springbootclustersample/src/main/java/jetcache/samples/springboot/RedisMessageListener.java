package jetcache.samples.springboot;

import java.util.Arrays;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

@Component
public class RedisMessageListener implements MessageListener {

    @Autowired
    private ConfigProvider configProvider;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        CacheMessageWithName cacheMessageWithName = (CacheMessageWithName)
                redisTemplate.getValueSerializer().deserialize(message.getBody());

        if (cacheMessageWithName.getKeys() == null) {
            return;
        }

        Cache localCache = (Cache) configProvider.getCacheManager().getCache(cacheMessageWithName.getArea(),
                cacheMessageWithName.getCacheName()).unwrap(Cache.class);

        if (localCache == null) {
            return;
        }

        if (RedisMessagePublisher.processId.equals(cacheMessageWithName.getProcessId())) {
            return;
        }

        switch (cacheMessageWithName.getType()) {
            case CacheMessage.TYPE_PUT:
            case CacheMessage.TYPE_REMOVE:
                localCache.invalidate(cacheMessageWithName.getKeys()[0]);
                break;
            case CacheMessage.TYPE_PUT_ALL:
            case CacheMessage.TYPE_REMOVE_ALL:
                localCache.invalidateAll(Arrays.asList(cacheMessageWithName.getKeys()));
                break;
        }

    }

}
