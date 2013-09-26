/**
 * Created on  13-09-22 16:54
 */
package com.taobao.geek.jetcache.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taobao.geek.jetcache.Cache;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheResult;
import com.taobao.geek.jetcache.CacheResultCode;

import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MockRemoteCache implements Cache {
    private HashMap<String, Value> data = new HashMap<String, Value>();

    @Override
    public CacheResult get(CacheConfig cacheConfig, String subArea, String key) {
        key = subArea + key;
        CacheResultCode code;
        Object value = null;
        try {
            Value cacheValue = data.get(key);
            if (cacheValue != null) {
                if (System.currentTimeMillis() - cacheValue.gmtCreate > cacheConfig.getExpire() * 1000) {
                    code = CacheResultCode.EXPIRED;
                } else {
                    code = CacheResultCode.SUCCESS;
                    value = JSON.parse(cacheValue.bytes);
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
    public CacheResultCode put(CacheConfig cacheConfig, String subArea, String key, Object value) {
        key = subArea + key;
        try {
            byte[] bytes = JSON.toJSONBytes(value, SerializerFeature.WriteClassName);
            Value v = new Value();
            v.bytes = bytes;
            v.gmtCreate = System.currentTimeMillis();
            data.put(key, v);
            return CacheResultCode.SUCCESS;
        } catch (Exception e) {
            return CacheResultCode.FAIL;
        }
    }

    private static class Value {
        byte[] bytes;
        long gmtCreate;
    }

}
