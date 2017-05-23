package com.alicp.jetcache;

import com.alicp.jetcache.event.CacheEvent;
import com.alicp.jetcache.event.CacheLoadAllEvent;
import com.alicp.jetcache.event.CacheLoadEvent;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2017/5/22.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class CacheUtil {

    public static <K, V> Function<K, V> createProxyLoader(Cache<K, V> cache,
                                                          K key,
                                                          Function<K, V> loader,
                                                          Consumer<CacheEvent> eventConsumer) {
        return (k) -> {
            long t = System.currentTimeMillis();
            V v = null;
            boolean success = false;
            try {
                v = loader.apply(k);
                success = true;
            } finally {
                t = System.currentTimeMillis() - t;
                CacheLoadEvent event = new CacheLoadEvent(cache, t, key, v, success);
                eventConsumer.accept(event);
            }
            return v;
        };
    }

    public static <K, V> Function<Set<K>, Map<K, V>> createProxyBatchLoader(Cache<K, V> cache,
                                                                                 Set<K> keys,
                                                                                 Function<Set<K>, Map<K, V>> batchLoader,
                                                                                 Consumer<CacheEvent> eventConsumer) {
        return (k) -> {
            long t = System.currentTimeMillis();
            boolean success = false;
            Map<K, V> kvMap = null;
            try {
                kvMap = batchLoader.apply(keys);
                success = true;
            } finally {
                t = System.currentTimeMillis() - t;
                CacheLoadAllEvent event = new CacheLoadAllEvent(cache, t, keys, kvMap, success);
                eventConsumer.accept(event);
            }
            return kvMap;
        };
    }
}
