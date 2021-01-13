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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
        cc.setPostCondition(anno.postCondition());
        cc.setSerialPolicy(anno.serialPolicy());
        cc.setKeyConvertor(anno.keyConvertor());
        cc.setKey(anno.key());
        cc.setDefineMethod(m);

        CacheRefresh cacheRefresh = m.getAnnotation(CacheRefresh.class);
        if (cacheRefresh != null) {
            RefreshPolicy policy = parseRefreshPolicy(cacheRefresh);
            cc.setRefreshPolicy(policy);
        }

        CachePenetrationProtect protectAnno = m.getAnnotation(CachePenetrationProtect.class);
        if (protectAnno != null) {
            PenetrationProtectConfig protectConfig = parsePenetrationProtectConfig(protectAnno);
            cc.setPenetrationProtectConfig(protectConfig);
        }

        return cc;
    }

    public static PenetrationProtectConfig parsePenetrationProtectConfig(CachePenetrationProtect protectAnno) {
        PenetrationProtectConfig protectConfig = new PenetrationProtectConfig();
        protectConfig.setPenetrationProtect(protectAnno.value());
        if (!CacheConsts.isUndefined(protectAnno.timeout())) {
            long timeout = protectAnno.timeUnit().toMillis(protectAnno.timeout());
            protectConfig.setPenetrationProtectTimeout(Duration.ofMillis(timeout));
        }
        return protectConfig;
    }

    public static RefreshPolicy parseRefreshPolicy(CacheRefresh cacheRefresh) {
        RefreshPolicy policy = new RefreshPolicy();
        TimeUnit t = cacheRefresh.timeUnit();
        policy.setRefreshMillis(t.toMillis(cacheRefresh.refresh()));
        if (!CacheConsts.isUndefined(cacheRefresh.stopRefreshAfterLastAccess())) {
            policy.setStopRefreshAfterLastAccessMillis(t.toMillis(cacheRefresh.stopRefreshAfterLastAccess()));
        }
        if (!CacheConsts.isUndefined(cacheRefresh.refreshLockTimeout())) {
            policy.setRefreshLockTimeoutMillis(t.toMillis(cacheRefresh.refreshLockTimeout()));
        }
        return policy;
    }

    public static List<CacheInvalidateAnnoConfig> parseCacheInvalidates(Method m) {
        List<CacheInvalidateAnnoConfig> annoList = null;
        CacheInvalidate ci = m.getAnnotation(CacheInvalidate.class);
        if (ci != null) {
            annoList = new ArrayList<>(1);
            annoList.add(createCacheInvalidateAnnoConfig(ci, m));
        } else {
            CacheInvalidateContainer cic = m.getAnnotation(CacheInvalidateContainer.class);
            if (cic != null) {
                CacheInvalidate[] cacheInvalidates = cic.value();
                annoList = new ArrayList<>(cacheInvalidates.length);
                for (CacheInvalidate cacheInvalidate : cacheInvalidates) {
                    annoList.add(createCacheInvalidateAnnoConfig(cacheInvalidate, m));
                }
            }
        }
        return annoList;
    }

    private static CacheInvalidateAnnoConfig createCacheInvalidateAnnoConfig(CacheInvalidate anno, Method m) {
        CacheInvalidateAnnoConfig cc = new CacheInvalidateAnnoConfig();
        cc.setArea(anno.area());
        cc.setName(anno.name());
        if (cc.getName() == null || cc.getName().trim().equals("")) {
            throw new CacheConfigException("name is required for @CacheInvalidate: " + m.getClass().getName() + "." + m.getName());
        }
        cc.setKey(anno.key());
        cc.setCondition(anno.condition());
        cc.setMulti(anno.multi());
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
        cc.setMulti(anno.multi());
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
        List<CacheInvalidateAnnoConfig> invalidateAnnoConfigs = parseCacheInvalidates(method);
        if (invalidateAnnoConfigs != null) {
            cac.setInvalidateAnnoConfigs(invalidateAnnoConfigs);
            hasAnnotation = true;
        }
        CacheUpdateAnnoConfig updateAnnoConfig = parseCacheUpdate(method);
        if (updateAnnoConfig != null) {
            cac.setUpdateAnnoConfig(updateAnnoConfig);
            hasAnnotation = true;
        }

        if (cachedConfig != null && (invalidateAnnoConfigs != null || updateAnnoConfig != null)) {
            throw new CacheConfigException("@Cached can't coexists with @CacheInvalidate or @CacheUpdate: " + method);
        }

        return hasAnnotation;
    }
}
