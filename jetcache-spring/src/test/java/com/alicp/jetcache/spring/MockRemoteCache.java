/**
 * Created on  13-09-22 16:54
 */
package com.alicp.jetcache.spring;

import com.alicp.jetcache.impl.CacheImplSupport;
import com.alicp.jetcache.support.Cache;
import com.alicp.jetcache.support.CacheConfig;
import com.alicp.jetcache.support.CacheResult;
import com.alicp.jetcache.support.CacheResultCode;

import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MockRemoteCache implements Cache {
    private HashMap<String, Value> data = new HashMap<String, Value>();

    public CacheResult get(CacheConfig cacheConfig, String subArea, String key) {
        key = subArea + key;
        CacheResultCode code;
        Object value = null;
        long expireTime = 0;
        try {
            Value cacheValue = data.get(key);
            if (cacheValue != null) {
                expireTime = cacheValue.expireTime;
                if (System.currentTimeMillis() > cacheValue.expireTime) {
                    code = CacheResultCode.EXPIRED;
                } else {
                    code = CacheResultCode.SUCCESS;
                    value = CacheImplSupport.decodeValue(cacheValue.bytes);
                }
            } else {
                code = CacheResultCode.NOT_EXISTS;
            }
        } catch (Exception e) {
            code = CacheResultCode.FAIL;
        }
        return new CacheResult(code, value, expireTime);
    }

    public CacheResultCode put(CacheConfig cacheConfig, String subArea, String key, Object value, long expireTime) {
        key = subArea + key;
        try {
            byte[] bytes = CacheImplSupport.encodeValue(value, cacheConfig.getSerialPolicy());
            Value v = new Value();
            v.bytes = bytes;
            v.expireTime = expireTime;
            data.put(key, v);
            return CacheResultCode.SUCCESS;
        } catch (Exception e) {
            return CacheResultCode.FAIL;
        }
    }

    private static class Value {
        byte[] bytes;
        long expireTime;
    }

}
