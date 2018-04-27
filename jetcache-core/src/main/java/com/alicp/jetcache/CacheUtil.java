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
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
class CacheUtil {

    public static <K, V> CacheLoader<K, V> createProxyLoader(Cache<K, V> cache,
                                                             CacheLoader<K, V> loader,
                                                             Consumer<CacheEvent> eventConsumer) {
        return new CacheLoader<K, V>() {
            @Override
            public V load(K key) throws Throwable {
                long t = System.currentTimeMillis();
                V v = null;
                boolean success = false;
                try {
                    v = loader.load(key);
                    success = true;
                } finally {
                    t = System.currentTimeMillis() - t;
                    CacheLoadEvent event = new CacheLoadEvent(cache, t, key, v, success);
                    eventConsumer.accept(event);
                }
                return v;
            }

            @Override
            public Map<K, V> loadAll(Set<K> keys) throws Throwable {
                long t = System.currentTimeMillis();
                boolean success = false;
                Map<K, V> kvMap = null;
                try {
                    kvMap = loader.loadAll(keys);
                    success = true;
                } finally {
                    t = System.currentTimeMillis() - t;
                    CacheLoadAllEvent event = new CacheLoadAllEvent(cache, t, keys, kvMap, success);
                    eventConsumer.accept(event);
                }
                return kvMap;
            }
        };
    }

    public static <K, V> Function<K, V> createProxyLoader(Cache<K, V> cache,
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
                CacheLoadEvent event = new CacheLoadEvent(cache, t, k, v, success);
                eventConsumer.accept(event);
            }
            return v;
        };
    }


    public static <K, V> AbstractCache<K, V> getAbstractCache(Cache<K, V> c) {
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return (AbstractCache) c;
    }

}
