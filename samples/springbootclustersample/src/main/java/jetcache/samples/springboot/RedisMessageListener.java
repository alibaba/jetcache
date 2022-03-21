package jetcache.samples.springboot;

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

        CacheMessage cacheMessage = cacheMessageWithName.getCacheMessage();

        if (cacheMessage == null) {
            return;
        }

        Cache localCache = (Cache) configProvider.getCacheManager().getCache(cacheMessageWithName.getArea(),
                cacheMessageWithName.getCacheName()).unwrap(Cache.class);

        if (localCache == null) {
            return;
        }

        switch (cacheMessage.getType()) {
            case CacheMessage.TYPE_REMOVE: {
                invalidateLocalCaches(localCache, cacheMessage.getKeys());
            }
            break;
            case CacheMessage.TYPE_REMOVE_ALL: {
                localCache.invalidateAll();
            }
            break;
        }

    }

    private void invalidateLocalCaches(Cache localCache, Object[] keys) {
        for (Object key : keys) {
            Object value = localCache.getIfPresent(key);
            if (null != value) {
                localCache.invalidate(key);
            }
        }
    }

}
