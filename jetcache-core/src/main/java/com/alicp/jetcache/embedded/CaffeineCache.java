package com.alicp.jetcache.embedded;

import com.alicp.jetcache.CacheValueHolder;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CaffeineCache<K, V> extends AbstractEmbeddedCache<K, V> {

    private com.github.benmanes.caffeine.cache.Cache cache;

    public CaffeineCache(EmbeddedCacheConfig<K, V> config) {
        super(config);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(com.github.benmanes.caffeine.cache.Cache.class)) {
            return (T) cache;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected InnerMap createAreaCache() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.maximumSize(config.getLimit());
        final boolean isExpireAfterAccess = config.isExpireAfterAccess();
        final long expireAfterAccess = config.getExpireAfterAccessInMillis();
        builder.expireAfter(new Expiry<Object, CacheValueHolder>() {
            private long getRestTimeInNanos(CacheValueHolder value) {
                long now = System.currentTimeMillis();
                long ttl = value.getExpireTime() - now;
                if(isExpireAfterAccess){
                    ttl = Math.min(ttl, expireAfterAccess);
                }
                return TimeUnit.MILLISECONDS.toNanos(ttl);
            }

            @Override
            public long expireAfterCreate(Object key, CacheValueHolder value, long currentTime) {
                return getRestTimeInNanos(value);
            }

            @Override
            public long expireAfterUpdate(Object key, CacheValueHolder value,
                                          long currentTime, long currentDuration) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(Object key, CacheValueHolder value,
                                        long currentTime, long currentDuration) {
                return getRestTimeInNanos(value);
            }
        });

        cache = builder.build();
        return new InnerMap() {
            @Override
            public Object getValue(Object key) {
                return cache.getIfPresent(key);
            }

            @Override
            public Map getAllValues(Collection keys) {
                return cache.getAllPresent(keys);
            }

            @Override
            public void putValue(Object key, Object value) {
                cache.put(key, value);
            }

            @Override
            public void putAllValues(Map map) {
                cache.putAll(map);
            }

            @Override
            public boolean removeValue(Object key) {
                return cache.asMap().remove(key) != null;
            }

            @Override
            public void removeAllValues(Collection keys) {
                cache.invalidateAll(keys);
            }

            @Override
            public boolean putIfAbsentValue(Object key, Object value) {
                return cache.asMap().putIfAbsent(key, value) == null;
            }
        };
    }
}
