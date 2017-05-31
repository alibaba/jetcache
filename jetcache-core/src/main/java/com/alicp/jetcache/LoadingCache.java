package com.alicp.jetcache;

import com.alicp.jetcache.event.CacheEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created on 2017/5/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LoadingCache<K, V> extends SimpleProxyCache<K, V> {

    protected Consumer<CacheEvent> eventConsumer;

    protected CacheConfig<K, V> config;

    public LoadingCache(Cache<K, V> cache) {
        super(cache);
        this.config = config();

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
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
            if (eventConsumer != null) {
                loader = CacheUtil.createProxyLoader(cache, loader, eventConsumer);
            }
            CacheGetResult<V> r = GET(key);
            if (r.isSuccess()) {
                return r.getValue();
            } else {
                Object loadedValue;
                try {
                    loadedValue = loader.load(key);
                } catch (Throwable e) {
                    throw new CacheInvokeException(e);
                }
                V castedValue = (V) loadedValue;
                if (loadedValue != null || config.isCacheNullValueByDefault()) {
                    put(key, castedValue);
                }
                return castedValue;
            }
        } else {
            return cache.get(key);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
            if (eventConsumer != null) {
                loader = CacheUtil.createProxyLoader(cache, loader, eventConsumer);
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
                Map<K, V> loadResult;
                try {
                    loadResult = loader.loadAll(keysNeedLoad);
                } catch (Throwable e) {
                    throw new CacheInvokeException(e);
                }

                kvMap.putAll(loadResult);
                return kvMap;
            } else {
                return new HashMap<>();
            }
        } else {
            return cache.getAll(keys);
        }

    }
}
