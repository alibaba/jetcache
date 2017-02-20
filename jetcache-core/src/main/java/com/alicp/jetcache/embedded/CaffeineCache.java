package com.alicp.jetcache.embedded;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CaffeineCache<K, V> extends AbstractEmbeddedCache<K, V> {

    private com.github.benmanes.caffeine.cache.Cache cache;

    public CaffeineCache(EmbeddedCacheConfig config) {
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
    protected InnerMap createAreaCache() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.maximumSize(config.getLimit());
        if (config.isExpireAfterAccess()) {
            builder.expireAfterAccess(config.getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
        } else {
            builder.expireAfterWrite(config.getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
        }
        cache = builder.build();
        return new InnerMap() {
            @Override
            public Object getValue(Object key) {
                return cache.getIfPresent(key);
            }

            @Override
            public List getAllValues(List keys) {
                Map m  = cache.getAllPresent((Iterable<?>) keys.iterator());
                List values = new ArrayList(keys.size());
                keys.stream().forEach((key) -> values.add(m.get(key)));
                return values;
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
            public void removeAllValues(Set keys) {
                cache.invalidateAll((Iterable<?>) keys.iterator());
            }

            @Override
            public boolean putIfAbsentValue(Object key, Object value) {
                return cache.asMap().putIfAbsent(key, value) == null;
            }
        };
    }
}
