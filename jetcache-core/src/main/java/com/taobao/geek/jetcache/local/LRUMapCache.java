/**
 * Created on  13-09-12 19:02
 */
package com.taobao.geek.jetcache.local;

import com.taobao.geek.jetcache.Cache;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheResult;
import com.taobao.geek.jetcache.CacheResultCode;
import org.apache.commons.collections.map.LRUMap;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yeli.hl
 */
public class LRUMapCache implements Cache {

    private HashMap<String, Map> areaMap = new HashMap<String, Map>(); //copy on write

    @Override
    public CacheResult get(CacheConfig cacheConfig, String subArea, String key) {
        Map<String, SoftReference<LRUMapCacheCacheObject>> map = getCacheMap(cacheConfig, subArea);
        SoftReference<LRUMapCacheCacheObject> ref = map.get(key);
        CacheResultCode code = CacheResultCode.FAIL;
        Object value = null;
        try {
            if (ref == null) {
                code = CacheResultCode.NOT_EXISTS;
            } else {
                LRUMapCacheCacheObject cacheValue = ref.get();
                if (cacheValue == null) {
                    code = CacheResultCode.NOT_EXISTS;
                } else {
                    if ((System.currentTimeMillis() - cacheValue.timestamp) / 1000 >= cacheConfig.getExpire()) {
                        code = CacheResultCode.EXPIRED;
                    } else {
                        code = CacheResultCode.SUCCESS;
                        value = cacheValue.value;
                    }
                }
            }
        } catch (Exception e) {
            code = CacheResultCode.FAIL;
        }
        return new CacheResult(code, value);
    }

    @Override
    public CacheResultCode put(CacheConfig cacheConfig, String subArea, String key, Object value) {
        Map<String, SoftReference<LRUMapCacheCacheObject>> map = getCacheMap(cacheConfig, subArea);
        LRUMapCacheCacheObject cacheValue = new LRUMapCacheCacheObject();
        cacheValue.value = value;
        cacheValue.timestamp = System.currentTimeMillis();
        SoftReference<LRUMapCacheCacheObject> ref = new SoftReference<LRUMapCacheCacheObject>(cacheValue);
        map.put(key, ref);
        return CacheResultCode.SUCCESS;
    }

    private Map<String, SoftReference<LRUMapCacheCacheObject>> getCacheMap(CacheConfig cacheConfig, String subArea) {
        StringBuilder sb = new StringBuilder();
        sb.append(cacheConfig.getArea());
        sb.append('_');
        sb.append(subArea);
        String areaKey = sb.toString();
        Map<String, SoftReference<LRUMapCacheCacheObject>> map = areaMap.get(areaKey);
        if (map == null) {
            map = Collections.synchronizedMap(new LRUMap(cacheConfig.getLocalLimit()));
            HashMap<String, Map> areaMapCopy = (HashMap<String, Map>) areaMap.clone();
            areaMapCopy.put(areaKey, map);
            areaMap = areaMapCopy;
        }
        return map;
    }

}

class LRUMapCacheCacheObject {
    Object value;
    long timestamp;
}

