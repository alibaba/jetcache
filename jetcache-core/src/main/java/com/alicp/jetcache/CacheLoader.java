package com.alicp.jetcache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created on 2017/5/27.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@FunctionalInterface
public interface CacheLoader<K, V> extends Function<K ,V> {
    V load(K key) throws Throwable;

    default Map<K, V> loadAll(Set<K> keys) throws Throwable {
        Map<K, V> map = new HashMap<>();
        for (K k : keys) {
            V value = load(k);
            if (value != null) {
                map.put(k, value);
            }
        }
        return map;
    }

    @Override
    default V apply(K key) {
        try {
            return load(key);
        } catch (Throwable e){
            throw new CacheInvokeException(e.getMessage(), e);
        }
    }

    default boolean vetoCacheUpdate() {
        return false;
    }

}
