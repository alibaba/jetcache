package com.alicp.jetcache;

import com.alicp.jetcache.event.CacheEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2017/5/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class LoadingCache<K, V> extends SimpleProxyCache<K, V> {

    private Consumer<CacheEvent> eventConsumer;

    public LoadingCache(Cache<K, V> cache) {
        super(cache);
        while (cache instanceof ProxyCache) {
            cache = ((ProxyCache) cache).getTargetCache();
        }
        final Cache c = cache;
        if (cache instanceof AbstractCache) {
            eventConsumer = (event) -> ((AbstractCache) c).notify(event);
        }
    }

    @Override
    public V get(K key) {
        CacheConfig<K, V> config = config();
        Function<K, V> loader = config.getLoader();
        Function<Set<K>, Map<K, V>> batchLoader = config.getBatchLoader();
        if (loader == null && batchLoader != null) {
            loader = (k) -> {
                Map<K, V> values = batchLoader.apply(Collections.singleton(key));
                if (values != null) {
                    return values.values().iterator().next();
                } else {
                    return null;
                }
            };
        }
        if (loader != null) {
            if (eventConsumer != null) {
                loader = CacheUtil.createProxyLoader(cache, loader, eventConsumer);
            }
            CacheGetResult<V> r = GET(key);
            if (r.isSuccess()) {
                return r.getValue();
            } else {
                Object loadedValue = loader.apply(key);
                V castedValue = (V) loadedValue;
                if (loadedValue != null || config.isCacheNullValueByDefault()) {
                    put(key, castedValue);
                }
                return castedValue;
            }
        } else {
            return super.get(key);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        CacheConfig<K, V> config = config();
        Function<K, V> loader = config.getLoader();
        Function<Set<K>, Map<K, V>> batchLoader = config.getBatchLoader();
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
        if (batchLoader != null) {
            if (eventConsumer != null) {
                batchLoader = CacheUtil.createProxyBatchLoader(cache, batchLoader, eventConsumer);
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
        } else {
            return getAll(keys);
        }

    }
}
