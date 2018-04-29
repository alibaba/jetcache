package com.alicp.jetcache;

import com.alicp.jetcache.event.CacheEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

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
        eventConsumer = CacheUtil.getAbstractCache(cache)::notify;
    }

    private Function<K, V> toLoaderFunction(CacheLoader<K, V> loader) {
        return (k) -> {
            try {
                return loader.load(k);
            } catch (Throwable e) {
                throw new CacheInvokeException(e);
            }
        };
    }

    @Override
    public V get(K key) throws CacheInvokeException {
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
            Function<K, V> loaderFunction = toLoaderFunction(loader);
            return AbstractCache.computeIfAbsentImpl(key, loaderFunction,
                    config.isCacheNullValue() ,0, null, this);
        } else {
            return cache.get(key);
        }
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) throws CacheInvokeException {
        CacheLoader<K, V> loader = config.getLoader();
        if (loader != null) {
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
            if (!config.isCachePenetrationProtect()) {
                if (eventConsumer != null) {
                    loader = CacheUtil.createProxyLoader(cache, loader, eventConsumer);
                }
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
            } else {
                AbstractCache<K, V> abstractCache = CacheUtil.getAbstractCache(cache);
                Function<K, V> loaderFunction = toLoaderFunction(loader);
                loaderFunction = CacheUtil.createProxyLoader(cache, loaderFunction, eventConsumer);
                for(K key : keysNeedLoad) {
                    Consumer<V> cacheUpdater = (v) -> PUT(key, v);
                    V v = AbstractCache.synchronizedLoad(abstractCache, key, loaderFunction,
                            cacheUpdater, abstractCache.initOrGetLoaderMap());
                    kvMap.put(key, v);
                }
            }
            return kvMap;
        } else {
            return cache.getAll(keys);
        }

    }
}
