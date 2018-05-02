/**
 * Created on  13-09-20 22:01
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.anno.*;
import com.alicp.jetcache.anno.support.CacheInvalidateAnnoConfig;
import com.alicp.jetcache.anno.support.CacheUpdateAnnoConfig;
import com.alicp.jetcache.anno.support.CachedAnnoConfig;
import com.alicp.jetcache.anno.support.PenetrationProtectConfig;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheConfigUtil {
    private static CachedAnnoConfig parseCached(Method m) {
        Cached anno = m.getAnnotation(Cached.class);
        if (anno == null) {
            return null;
        }
        CachedAnnoConfig cc = new CachedAnnoConfig();
        cc.setArea(anno.area());
        cc.setName(anno.name());
        cc.setCacheType(anno.cacheType());
        cc.setEnabled(anno.enabled());
        cc.setTimeUnit(anno.timeUnit());
        cc.setExpire(anno.expire());
        cc.setLocalExpire(anno.localExpire());
        cc.setLocalLimit(anno.localLimit());
        cc.setCacheNullValue(anno.cacheNullValue());
        cc.setCondition(anno.condition());
        cc.setSerialPolicy(anno.serialPolicy());
        cc.setKeyConvertor(anno.keyConvertor());
        cc.setKey(anno.key());
        cc.setDefineMethod(m);

        CacheRefresh cacheRefresh = m.getAnnotation(CacheRefresh.class);
        if (cacheRefresh != null) {
            RefreshPolicy policy = parseRefreshPolicy(cacheRefresh);
            cc.setRefreshPolicy(policy);
        }

        CachePenetrationProtect penetrateProtect = m.getAnnotation(CachePenetrationProtect.class);
        if (penetrateProtect != null) {
            PenetrationProtectConfig protectConfig = new PenetrationProtectConfig();
            protectConfig.setPenetrationProtect(penetrateProtect.value());
            cc.setPenetrationProtectConfig(protectConfig);
        }

        return cc;
    }

    public static RefreshPolicy parseRefreshPolicy(CacheRefresh cacheRefresh) {
        RefreshPolicy policy = new RefreshPolicy();
        TimeUnit t = cacheRefresh.timeUnit();
        policy.setRefreshMillis(t.toMillis(cacheRefresh.refresh()));
        if (!CacheConsts.isUndefined(cacheRefresh.stopRefreshAfterLastAccess())) {
            policy.setStopRefreshAfterLastAccessMillis(t.toMillis(cacheRefresh.stopRefreshAfterLastAccess()));
        }
        if(!CacheConsts.isUndefined(cacheRefresh.refreshLockTimeout())){
            policy.setRefreshLockTimeoutMillis(t.toMillis(cacheRefresh.refreshLockTimeout()));
        }
        return policy;
    }

    private static CacheInvalidateAnnoConfig parseCacheInvalidate(Method m) {
        CacheInvalidate anno = m.getAnnotation(CacheInvalidate.class);
        if (anno == null) {
            return null;
        }
        CacheInvalidateAnnoConfig cc = new CacheInvalidateAnnoConfig();
        cc.setArea(anno.area());
        cc.setName(anno.name());
        if (cc.getName() == null || cc.getName().trim().equals("")) {
            throw new CacheConfigException("name is required for @CacheInvalidate: " + m.getClass().getName() + "." + m.getName());
        }
        cc.setKey(anno.key());
        cc.setCondition(anno.condition());
        cc.setDefineMethod(m);
        return cc;
    }

    private static CacheUpdateAnnoConfig parseCacheUpdate(Method m) {
        CacheUpdate anno = m.getAnnotation(CacheUpdate.class);
        if (anno == null) {
            return null;
        }
        CacheUpdateAnnoConfig cc = new CacheUpdateAnnoConfig();
        cc.setArea(anno.area());
        cc.setName(anno.name());
        if (cc.getName() == null || cc.getName().trim().equals("")) {
            throw new CacheConfigException("name is required for @CacheUpdate: " + m.getClass().getName() + "." + m.getName());
        }
        cc.setKey(anno.key());
        cc.setValue(anno.value());
        if (cc.getValue() == null || cc.getValue().trim().equals("")) {
            throw new CacheConfigException("value is required for @CacheUpdate: " + m.getClass().getName() + "." + m.getName());
        }
        cc.setCondition(anno.condition());
        cc.setDefineMethod(m);
        return cc;
    }


    private static boolean parseEnableCache(Method m) {
        EnableCache anno = m.getAnnotation(EnableCache.class);
        return anno != null;
    }

    public static boolean parse(CacheInvokeConfig cac, Method method) {
        boolean hasAnnotation = false;
        CachedAnnoConfig cachedConfig = parseCached(method);
        if (cachedConfig != null) {
            cac.setCachedAnnoConfig(cachedConfig);
            hasAnnotation = true;
        }
        boolean enable = parseEnableCache(method);
        if (enable) {
            cac.setEnableCacheContext(true);
            hasAnnotation = true;
        }
        CacheInvalidateAnnoConfig invalidateAnnoConfig = parseCacheInvalidate(method);
        if (invalidateAnnoConfig != null) {
            cac.setInvalidateAnnoConfig(invalidateAnnoConfig);
            hasAnnotation = true;
        }
        CacheUpdateAnnoConfig updateAnnoConfig = parseCacheUpdate(method);
        if (updateAnnoConfig != null) {
            cac.setUpdateAnnoConfig(updateAnnoConfig);
            hasAnnotation = true;
        }

        if (cachedConfig != null && (invalidateAnnoConfig != null || updateAnnoConfig != null)) {
            throw new CacheConfigException("@Cached can't coexists with @CacheInvalidate or @CacheUpdate: " + method);
        }

        return hasAnnotation;
    }
}
