/**
 * Created on  13-09-22 16:54
 */
package com.alicp.jetcache.testsupport;

import com.alicp.jetcache.CacheConsts;
import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.impl.CacheImplSupport;
import com.alicp.jetcache.support.CacheResult;
import com.alicp.jetcache.support.CacheResultCode;

import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MockRemoteCache<K, V> implements Cache<K, V> {
    private HashMap<K, ValueHolder> data = new HashMap();

    @Override
    public String getSubArea() {
        return "mock";
    }

    public CacheResult<V> GET(K key) {
        CacheResultCode code;
        V value = null;
        long expireTime = 0;
        try {
            ValueHolder holder = data.get(key);
            if (holder != null) {
                expireTime = holder.expireTime;
                if (System.currentTimeMillis() > holder.expireTime) {
                    code = CacheResultCode.EXPIRED;
                } else {
                    code = CacheResultCode.SUCCESS;
                    value = (V)CacheImplSupport.decodeValue(holder.bytes);
                }
            } else {
                code = CacheResultCode.NOT_EXISTS;
            }
        } catch (Exception e) {
            code = CacheResultCode.FAIL;
        }
        return new CacheResult(code, value);
    }

    @Override
    public void put(K key, V value) {
        PUT(key, value, CacheConsts.DEFAULT_EXPIRE);
    }

    public CacheResultCode PUT(K key, V value, int expireTime) {
        try {
            byte[] bytes = CacheImplSupport.encodeValue(value, CacheConsts.DEFAULT_SERIAL_POLICY);
            ValueHolder v = new ValueHolder();
            v.bytes = bytes;
            v.expireTime = expireTime;
            data.put(key, v);
            return CacheResultCode.SUCCESS;
        } catch (Exception e) {
            return CacheResultCode.FAIL;
        }
    }

    @Override
    public CacheResultCode INVALIDATE(K key) {
        data.remove(key);
        return CacheResultCode.SUCCESS;
    }

    private static class ValueHolder {
        byte[] bytes;
        long expireTime;
    }

}
