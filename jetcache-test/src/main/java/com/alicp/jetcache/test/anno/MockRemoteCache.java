/**
 * Created on  13-09-22 16:54
 */
package com.alicp.jetcache.test.anno;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.method.SerializeUtil;
import com.alicp.jetcache.embedded.SimpleLock;
import com.alicp.jetcache.external.ExternalCacheConfig;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MockRemoteCache<K, V> implements Cache<K, V> {
    private HashMap<Object, CacheValueHolder<byte[]>> data = new HashMap();
    private ExternalCacheConfig config;

    public MockRemoteCache(ExternalCacheConfig config) {
        this.config = config;
    }

    @Override
    public CacheConfig config() {
        return config;
    }

    private Object buildKey(K key) {
        return config().getKeyConvertor().apply(key);
    }

    public synchronized CacheGetResult<V> GET(K key) {
        CacheResultCode code;
        V value = null;
        try {
            CacheValueHolder<byte[]> holder = data.get(buildKey(key));
            if (holder != null) {
                long expireTime = holder.getExpireTime();
                if (System.currentTimeMillis() >= expireTime) {
                    code = CacheResultCode.EXPIRED;
                } else {
                    code = CacheResultCode.SUCCESS;
                    value = (V) config.getValueDecoder().apply(holder.getValue());
                }
            } else {
                code = CacheResultCode.NOT_EXISTS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            code = CacheResultCode.FAIL;
        }
        return new CacheGetResult(code, null, value);
    }

    @Override
    public synchronized CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        try {
            byte[] bytes = config.getValueEncoder().apply(value);
            CacheValueHolder<byte[]> v = new CacheValueHolder(bytes, System.currentTimeMillis(), timeUnit.toMillis(expire));
            data.put(buildKey(key), v);
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Exception e) {
            e.printStackTrace();
            return CacheResult.FAIL_WITHOUT_MSG;
        }
    }

    @Override
    public synchronized CacheResult REMOVE(K key) {
        data.remove(buildKey(key));
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }

    @Override
    public AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit) {
        return SimpleLock.tryLock(this, key, expire, timeUnit);
    }

    @Override
    public synchronized CacheResult PUT_IF_ABSENT(K key, V value, long expire, TimeUnit timeUnit) {
        if (get(key) == null) {
            put(key, value, expire, timeUnit);
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } else {
            return CacheResult.EXISTS_WITHOUT_MSG;
        }
    }
}
