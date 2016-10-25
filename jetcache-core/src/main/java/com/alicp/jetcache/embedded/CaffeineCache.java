package com.alicp.jetcache.embedded;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CaffeineCache extends AbstractEmbeddedCache {

    public CaffeineCache(EmbeddedCacheConfig config) {
        super(config);
    }

    @Override
    protected IntenalMap createAreaCache() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.maximumSize(config.getLimit());
        if (config.isExpireAfterAccess()) {
            builder.expireAfterAccess(config.getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
        } else {
            builder.expireAfterWrite(config.getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
        }
        com.github.benmanes.caffeine.cache.Cache cache = builder.build();
        return new IntenalMap() {
            @Override
            public Object getValue(Object key) {
                return cache.getIfPresent(key);
            }

            @Override
            public void putValue(Object key, Object value) {
                cache.put(key, value);
            }

            @Override
            public void removeValue(Object key) {
                cache.invalidate(key);
            }
        };
    }
}
