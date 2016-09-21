/**
 * Created on  13-09-20 22:01
 */
package com.alicp.jetcache.impl;

import com.alicp.jetcache.support.CacheConfig;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfigUtil {
    private static CacheConfig parseCacheConfig(Method m) {
        Cached anno = m.getAnnotation(Cached.class);
        if (anno == null) {
            return null;
        }
        CacheConfig cc = new CacheConfig();
        cc.setArea(anno.area());
        cc.setCacheType(anno.cacheType());
        cc.setEnabled(anno.enabled());
        cc.setExpire(anno.expire());
        cc.setLocalLimit(anno.localLimit());
        cc.setVersion(anno.version());
        cc.setCacheNullValue(anno.cacheNullValue());
        cc.setCondition(anno.condition());
        cc.setUnless(anno.unless());
        cc.setSerialPolicy(anno.serialPolicy());
        return cc;
    }

    private static boolean parseEnableCacheConfig(Method m) {
        EnableCache anno = m.getAnnotation(EnableCache.class);
        return anno != null;
    }

    public static boolean parse(CacheInvokeConfig cac, Method method) {
        CacheConfig cc = parseCacheConfig(method);
        if (cc != null) {
            cac.setCacheConfig(cc);
            cac.init();
        }
        boolean enable = parseEnableCacheConfig(method);
        if (enable) {
            cac.setEnableCacheContext(true);
        }
        return cc != null || enable;
    }
}
