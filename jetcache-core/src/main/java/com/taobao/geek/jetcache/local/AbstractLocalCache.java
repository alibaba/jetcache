/**
 * Created on  13-10-17 23:01
 */
package com.taobao.geek.jetcache.local;

import com.taobao.geek.jetcache.support.Cache;
import com.taobao.geek.jetcache.support.CacheConfig;
import com.taobao.geek.jetcache.support.CacheResult;
import com.taobao.geek.jetcache.support.CacheResultCode;
import com.taobao.geek.jetcache.util.CopyOnWriteHashMap;

import java.lang.ref.SoftReference;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractLocalCache implements Cache {
    protected boolean useSoftRef = false;
    protected CopyOnWriteHashMap<String, AreaCache> areaMap = new CopyOnWriteHashMap<String, AreaCache>();

    protected abstract AreaCache getCacheMap(CacheConfig cacheConfig, String subArea);

    public AbstractLocalCache(){
    }

    public AbstractLocalCache(boolean useSoftRef){
        this.useSoftRef = useSoftRef;
    }

    @Override
    public CacheResult get(CacheConfig cacheConfig, String subArea, String key) {
        CacheResultCode code = CacheResultCode.FAIL;
        Object value = null;
        try {
            AreaCache map = getCacheMap(cacheConfig, subArea);

            if (useSoftRef) {
                SoftReference<CacheObject> ref = (SoftReference<CacheObject>) map.getValue(key);
                if (ref == null) {
                    code = CacheResultCode.NOT_EXISTS;
                } else {
                    CacheObject cacheObject = ref.get();
                    if (cacheObject == null) {
                        code = CacheResultCode.NOT_EXISTS;
                    } else {
                        if (System.currentTimeMillis() - cacheObject.expireTime >= 0) {
                            map.removeValue(key);
                            code = CacheResultCode.EXPIRED;
                        } else {
                            code = CacheResultCode.SUCCESS;
                            value = cacheObject.value;
                        }
                    }
                }
            } else {
                CacheObject cacheObject = (CacheObject) map.getValue(key);
                if (cacheObject == null) {
                    code = CacheResultCode.NOT_EXISTS;
                } else {
                    if (System.currentTimeMillis() - cacheObject.expireTime >= 0) {
                        map.removeValue(key);
                        code = CacheResultCode.EXPIRED;
                    } else {
                        code = CacheResultCode.SUCCESS;
                        value = cacheObject.value;
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
        AreaCache map = getCacheMap(cacheConfig, subArea);
        CacheObject cacheObject = new CacheObject();
        cacheObject.value = value;
        cacheObject.expireTime = System.currentTimeMillis() + cacheConfig.getExpire() * 1000L;
        if (useSoftRef) {
            SoftReference<CacheObject> ref = new SoftReference<CacheObject>(cacheObject);
            map.putValue(key, ref);
        } else {
            map.putValue(key, cacheObject);
        }
        return CacheResultCode.SUCCESS;
    }

    public boolean isUseSoftRef() {
        return useSoftRef;
    }

}
