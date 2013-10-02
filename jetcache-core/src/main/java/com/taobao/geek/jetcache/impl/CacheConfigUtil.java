/**
 * Created on  13-09-20 22:01
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.Cached;
import com.taobao.geek.jetcache.EnableCache;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfigUtil {
    public static CacheConfig parseCacheConfig(Method m) {
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
        return cc;
    }

    public static boolean parseEnableCacheConfig(Method m) {
        EnableCache anno = m.getAnnotation(EnableCache.class);
        return anno != null;
    }

    public static void parse(CacheAnnoConfig cac, Method method) {
        CacheConfig cc = parseCacheConfig(method);
        if (cc != null) {
            cac.setCacheConfig(cc);
        }
        boolean enable = parseEnableCacheConfig(method);
        if (enable) {
            cac.setEnableCacheContext(true);
        }
    }
}
