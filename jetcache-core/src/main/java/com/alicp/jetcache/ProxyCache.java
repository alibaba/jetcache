package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface ProxyCache<K, V> extends ConfigAwareCache<K, V> {
    Cache<K, V> getTargetCache();

    @Override
    default CacheConfig<K, V> config() {
        if (getTargetCache() instanceof ConfigAwareCache) {
            return ((ConfigAwareCache) getTargetCache()).config();
        } else {
            return null;
        }
    }

    @Override
    default <T> T unwrap(Class<T> clazz) {
        return getTargetCache().unwrap(clazz);
    }

}
