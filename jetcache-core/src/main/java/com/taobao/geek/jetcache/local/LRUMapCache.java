/**
 * Created on  13-09-12 19:02
 */
package com.taobao.geek.jetcache.local;

import com.taobao.geek.jetcache.support.Cache;
import com.taobao.geek.jetcache.support.CacheConfig;
import com.taobao.geek.jetcache.support.CacheResult;
import com.taobao.geek.jetcache.support.CacheResultCode;
import com.taobao.geek.jetcache.util.CopyOnWriteHashMap;
import org.apache.commons.collections.map.LRUMap;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LRUMapCache implements Cache {

    private CopyOnWriteHashMap<String, Map> areaMap = new CopyOnWriteHashMap<String, Map>();

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
                    if (System.currentTimeMillis() - cacheValue.expireTime >= 0) {
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
        cacheValue.expireTime = System.currentTimeMillis() + cacheConfig.getExpire() * 1000;
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
            map = new LRUMap(cacheConfig.getLocalLimit());
            map = Collections.synchronizedMap(map);
            areaMap.put(areaKey, map);
        }
        return map;
    }

}

