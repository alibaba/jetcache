package com.alicp.jetcache;

/**
 * Created on 2016/12/13.
 *
 * @author huangli
 */
public interface ProxyCache<K, V> extends Cache<K, V> {
    Cache<K, V> getTargetCache();

    @Override
    default <T> T unwrap(Class<T> clazz) {
        return getTargetCache().unwrap(clazz);
    }

}
