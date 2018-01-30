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
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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
        if (cache instanceof AbstractCache) {
            eventConsumer = ((AbstractCache) cache)::notify;
        }
    }

    @Override
    public V get(K key) throws CacheInvokeException {
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
            if (eventConsumer != null) {
                loader = CacheUtil.createProxyLoader(cache, loader, eventConsumer);
            }
            CacheGetResult<V> r = GET(key);
            if (r.isSuccess()) {
                return r.getValue();
            } else {
                V loadedValue;
                try {
                    loadedValue = loader.load(key);
                } catch (Throwable e) {
                    throw new CacheInvokeException(e);
                }
                if (loadedValue != null || config.isCacheNullValue()) {
                    PUT(key, loadedValue);
                }
                return loadedValue;
            }
        } else {
            return cache.get(key);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) throws CacheInvokeException {
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
            if (eventConsumer != null) {
                loader = CacheUtil.createProxyLoader(cache, loader, eventConsumer);
            }
            MultiGetResult<K, V> r = GET_ALL(keys);
            Map<K, V> kvMap;
            if (r.isSuccess() || r.getResultCode() == CacheResultCode.PART_SUCCESS) {
                kvMap = r.unwrapValues();
            } else {
                kvMap = new HashMap<>();
            }
            Set<K> keysNeedLoad = new HashSet<>();
            keys.forEach((k) -> {
                if (!kvMap.containsKey(k)) {
                    keysNeedLoad.add(k);
                }
            });
            Map<K, V> loadResult;
            try {
                loadResult = loader.loadAll(keysNeedLoad);
                for (Map.Entry<K, V> en : loadResult.entrySet()) {
                    K key = en.getKey();
                    V loadedValue = en.getValue();
                    if (loadedValue != null || config.isCacheNullValue()) {
                        PUT(key, loadedValue);
                    }
                }
            } catch (Throwable e) {
                throw new CacheInvokeException(e);
            }
            kvMap.putAll(loadResult);
            return kvMap;
        } else {
            return cache.getAll(keys);
        }

    }
}
