/**
 * Created on  13-09-09 15:59
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.context.CacheContext;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheHandler implements InvocationHandler {

    private Object src;
    private GlobalCacheConfig globalCacheConfig;

    private CacheInvokeConfig cacheInvokeConfig;
    private HashMap<String, CacheInvokeConfig> configMap;

    private static class CacheContextSupport extends CacheContext {
        void _enable() {
            enable();
        }

        void _disable() {
            disable();
        }

        boolean _isEnabled() {
            return isEnabled();
        }
    }

    private static CacheContextSupport cacheContextSupport = new CacheContextSupport();

    public CacheHandler(Object src, CacheAnnoConfig cacheAnnoConfig, GlobalCacheConfig globalCacheConfig) {
        this.src = src;
        cacheInvokeConfig = new CacheInvokeConfig();
        cacheInvokeConfig.cacheAnnoConfig = cacheAnnoConfig;
        cacheInvokeConfig.init();
        this.globalCacheConfig = globalCacheConfig;
    }

    public CacheHandler(Object src, HashMap<String, CacheInvokeConfig> configMap, GlobalCacheConfig globalCacheConfig) {
        this.src = src;
        this.configMap = configMap;
        this.globalCacheConfig = globalCacheConfig;
    }

    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        CacheInvokeContext context = null;
        if (cacheInvokeConfig != null) {
            context = globalCacheConfig.createCacheInvokeContext();
            context.cacheInvokeConfig = cacheInvokeConfig;
        } else {
            String sig = ClassUtil.getMethodSig(method, globalCacheConfig.getHidePackages());
            CacheInvokeConfig cac = configMap.get(sig);
            if (cac != null) {
                context = globalCacheConfig.createCacheInvokeContext();
                context.cacheInvokeConfig = cac;
            }
        }
        if (context == null) {
            return method.invoke(src, args);
        } else {
            context.args = args;
            context.globalCacheConfig = globalCacheConfig;
            context.method = method;
            context.target = src;
            return invoke(context);
        }
    }

    public static Object invoke(CacheInvokeContext context) throws Throwable {
        if (context.cacheInvokeConfig.enableCacheContext) {
            try {
                cacheContextSupport._enable();
                return doInvoke(context);
            } finally {
                cacheContextSupport._disable();
            }
        } else {
            return doInvoke(context);
        }
    }

    private static Object doInvoke(CacheInvokeContext context) throws Throwable {
        CacheAnnoConfig cacheAnnoConfig = context.cacheInvokeConfig.cacheAnnoConfig;
        if (cacheAnnoConfig != null && (cacheAnnoConfig.isEnabled() || cacheContextSupport._isEnabled())) {
            return invokeWithCache(context);
        } else {
            return invokeOrigin(context);
        }
    }

    private static Object invokeWithCache(CacheInvokeContext context)
            throws Throwable {
        if (!ExpressionUtil.evalCondition(context)) {
            return invokeOrigin(context);
        }

        CacheAnnoConfig cacheAnnoConfig = context.cacheInvokeConfig.cacheAnnoConfig;

        String subArea = ClassUtil.getSubArea(cacheAnnoConfig, context.method, context.globalCacheConfig.getHidePackages());

        Cache cache = context.globalCacheConfig.getCacheManager().getCache(subArea);

        cache.computeIfAbsent(context.args, (key) -> {
            try {
                context.result = invokeOrigin(context);
                if (ExpressionUtil.evalUnless(context)) {
                    // don't cache it
                    return null;
                } else {
                    return context.result;
                }
            } catch (Throwable e) {
                context.exception = e;
                return null;
            }
        }, cacheAnnoConfig.isCacheNullValue());
        if (context.exception != null) {
            throw context.exception;
        }

        return context.result;
    }

    private static Object invokeOrigin(CacheInvokeContext context) throws Throwable {
        if (context.invoker == null) {
            return context.method.invoke(context.target, context.args);
        } else {
            return context.invoker.invoke();
        }
    }


}
