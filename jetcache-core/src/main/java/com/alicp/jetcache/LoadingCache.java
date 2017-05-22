package com.alicp.jetcache;

import java.util.*;
import java.util.function.Function;

/**
 * Created on 2017/5/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class LoadingCache<K, V> extends SimpleProxyCache<K, V> {

    public LoadingCache(Cache<K, V> cache) {
        super(cache);
    }

    @Override
    public V get(K key) {
        CacheConfig<K, V> config = config();
        Function<K, V> loader = config.getLoader();
        Function<Iterable<K>, Map<K, V>> batchLoader = config.getBatchLoader();
        if (loader == null && batchLoader != null) {
            loader = (k) -> {
                Map<K, V> values = batchLoader.apply(Arrays.asList(k));
                if (values != null) {
                    return values.values().iterator().next();
                } else {
                    return null;
                }
            };
        }
        if (loader != null) {
            return computeIfAbsent(key, loader);
        } else {
            return super.get(key);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        CacheConfig<K, V> config = config();
        Function<K, V> loader = config.getLoader();
        Function<Iterable<K>, Map<K, V>> batchLoader = config.getBatchLoader();
        if (batchLoader == null && loader != null) {
            batchLoader = (ks) -> {
                Map<K, V> map = new HashMap<>();
                ks.forEach((k) -> {
                    V value = loader.apply(k);
                    if (value != null) {
                        map.put(k, value);
                    }
                });
                return map;
            };
        }
        MultiGetResult<K, V> r = GET_ALL(keys);
        if (r.isSuccess() || r.getResultCode() == CacheResultCode.PART_SUCCESS) {
            Map<K, V> kvMap = r.unwrapValues();
            Set<K> keysNeedLoad = new HashSet<>();
            keys.forEach((k) -> {
                if (!kvMap.containsKey(k)) {
                    keysNeedLoad.add(k);
                }
            });
            Map<K, V> loadResult = batchLoader.apply(keysNeedLoad);
            kvMap.putAll(loadResult);
            return kvMap;
        } else {
            return new HashMap<>();
        }

    }
}
