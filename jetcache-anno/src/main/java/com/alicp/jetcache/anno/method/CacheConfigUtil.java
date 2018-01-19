/**
 * Created on  13-09-20 22:01
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheConfigUtil {
    private static CacheAnnoConfig parseCacheConfig(Method m) {
        Cached anno = m.getAnnotation(Cached.class);
        if (anno == null) {
            return null;
        }
        CacheAnnoConfig cc = new CacheAnnoConfig();
        cc.setArea(anno.area());
        cc.setName(anno.name());
        cc.setCacheType(anno.cacheType());
        cc.setEnabled(anno.enabled());
        cc.setTimeUnit(anno.timeUnit());
        cc.setExpire(anno.expire());
        cc.setLocalLimit(anno.localLimit());
        cc.setCacheNullValue(anno.cacheNullValue());
        cc.setCondition(anno.condition());
        cc.setUnless(anno.unless());
        cc.setSerialPolicy(anno.serialPolicy());
        cc.setKeyConvertor(anno.keyConvertor());
        cc.setKey(anno.key());
        return cc;
    }

    private static boolean parseEnableCacheConfig(Method m) {
        EnableCache anno = m.getAnnotation(EnableCache.class);
        return anno != null;
    }

    public static boolean parse(CacheInvokeConfig cac, Method method) {
        CacheAnnoConfig cc = parseCacheConfig(method);
        if (cc != null) {
            cac.setCacheAnnoConfig(cc);
        }
        boolean enable = parseEnableCacheConfig(method);
        if (enable) {
            cac.setEnableCacheContext(true);
        }
        return cc != null || enable;
    }
}
