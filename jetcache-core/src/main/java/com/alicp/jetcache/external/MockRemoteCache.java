/**
 * Created on  13-09-22 16:54
 */
package com.alicp.jetcache.external;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.MultiGetResult;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author huangli
 */
public class MockRemoteCache<K, V> extends AbstractExternalCache<K, V> {
    private Cache<ByteBuffer, byte[]> cache;
    private ExternalCacheConfig<K, V> config;

    public MockRemoteCache(MockRemoteCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(config.getLimit())
                .expireAfterWrite(config.getExpireAfterWriteInMillis(), TimeUnit.MILLISECONDS)
                .buildCache();
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    private ByteBuffer genKey(K key) {
        return ByteBuffer.wrap(buildKey(key));
    }

    private ByteBuffer genKey(byte[] keyBytes) {
        return ByteBuffer.wrap(keyBytes);
    }

    //-------------------------------


    @Override
    public <T> T unwrap(Class<T> clazz) {
        return cache.unwrap(clazz);
    }


    private static Method getHolder;

    static {
        try {
            getHolder = CacheGetResult.class.getDeclaredMethod("getHolder");
            getHolder.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new CacheException(e);
        }
    }

    private CacheGetResult convertCacheGetResult(CacheGetResult originResult) {
        try {
            CacheValueHolder originHolder = (CacheValueHolder) getHolder.invoke(originResult);
            LinkedList<CacheValueHolder> list = new LinkedList<>();
            while (originHolder != null) {
                CacheValueHolder h = new CacheValueHolder();
                if (list.size() > 0) {
                    list.getLast().setValue(h);
                }
                list.add(h);
                h.setAccessTime(originHolder.getAccessTime());
                h.setExpireTime(originHolder.getExpireTime());

                Object v = originHolder.getValue();
                if (v != null && !(v instanceof CacheValueHolder)) {
                    h.setValue(config.getValueDecoder().apply((byte[]) v));
                    break;
                } else if (originHolder.getValue() == null) {
                    originHolder = (CacheValueHolder) originHolder.getValue();
                }
            }
            return new CacheGetResult(originResult.getResultCode(), originResult.getMessage(), list.peekFirst());
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public CacheValueHolder getHolder(K key) {
        try {
            CacheGetResult<V> r = GET(key);
            return (CacheValueHolder) getHolder.invoke(r);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        CacheGetResult r = cache.GET(genKey(key));
        if (r.isSuccess()) {
            r = convertCacheGetResult(r);
        }
        return r;
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        ArrayList<K> keyList = new ArrayList<>(keys.size());
        ArrayList<ByteBuffer> newKeyList = new ArrayList<>(keys.size());
        keys.stream().forEach((k) -> {
            ByteBuffer newKey = genKey(k);
            keyList.add(k);
            newKeyList.add(newKey);
        });
        MultiGetResult<ByteBuffer, byte[]> result = cache.GET_ALL(new HashSet(newKeyList));
        Map<ByteBuffer, CacheGetResult<byte[]>> resultMap = result.getValues();
        if (resultMap != null) {
            Map<K, CacheGetResult<V>> returnMap = new HashMap<>();
            for (int i = 0; i < keyList.size(); i++) {
                K key = keyList.get(i);
                ByteBuffer newKey = newKeyList.get(i);
                CacheGetResult r = resultMap.get(newKey);
                if (r.getValue() != null) {
                    r = convertCacheGetResult(r);
                }
                returnMap.put(key, r);
            }
            result = new MultiGetResult<ByteBuffer, byte[]>(result.getResultCode(), null, (Map) returnMap);
        }
        return (MultiGetResult) result;
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        byte[] keyBytes = buildKey(key);
        byte[] valueBytes = config.getValueEncoder().apply(value);
        executeWriteInterceptors(key, value, keyBytes, valueBytes, expireAfterWrite, timeUnit, ExternalCacheWriteInterceptor.WriteContext.Op.PUT);
        return cache.PUT(genKey(key), valueBytes, expireAfterWrite, timeUnit);
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        Map<ByteBuffer, byte[]> newMap = new HashMap<>();
        map.entrySet().forEach((e) -> {
                    byte[] keyBytes = buildKey(e.getKey());
                    byte[] valueBytes = config.getValueEncoder().apply(e.getValue());
                    executeWriteInterceptors(e.getKey(), e.getValue(), keyBytes, valueBytes, expireAfterWrite, timeUnit, ExternalCacheWriteInterceptor.WriteContext.Op.PUT_ALL);
                    newMap.put(genKey(keyBytes), valueBytes);
                }
        );
        return cache.PUT_ALL(newMap, expireAfterWrite, timeUnit);
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        return cache.REMOVE(genKey(key));
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        return cache.REMOVE_ALL(keys.stream().map((k) -> genKey(k)).collect(Collectors.toSet()));
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        byte[] keyBytes = buildKey(key);
        byte[] valueBytes = config.getValueEncoder().apply(value);
        executeWriteInterceptors(key, value, keyBytes, valueBytes, expireAfterWrite, timeUnit, ExternalCacheWriteInterceptor.WriteContext.Op.PUT_IF_ABSENT);
        return cache.PUT_IF_ABSENT(genKey(keyBytes), valueBytes, expireAfterWrite, timeUnit);
    }
}
