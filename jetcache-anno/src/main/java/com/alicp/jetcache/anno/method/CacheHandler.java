/**
 * Created on  13-09-09 15:59
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.support.CachedAnnoConfig;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.ConfigMap;
import com.alicp.jetcache.event.CacheLoadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheHandler implements InvocationHandler {
    private static Logger logger = LoggerFactory.getLogger(CacheHandler.class);

    private Object src;
    private Supplier<CacheInvokeContext> contextSupplier;
    private String[] hiddenPackages;
    private ConfigMap configMap;

    private static class CacheContextSupport extends CacheContext {

        public CacheContextSupport() {
            super(null);
        }

        static void _enable() {
            enable();
        }

        static void _disable() {
            disable();
        }

        static boolean _isEnabled() {
            return isEnabled();
        }
    }

    public CacheHandler(Object src, ConfigMap configMap, Supplier<CacheInvokeContext> contextSupplier, String[] hiddenPackages) {
        this.src = src;
        this.configMap = configMap;
        this.contextSupplier = contextSupplier;
        this.hiddenPackages = hiddenPackages;
    }

    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        CacheInvokeContext context = null;

        String sig = ClassUtil.getMethodSig(method);
        CacheInvokeConfig cac = configMap.getByMethodInfo(sig);
        if (cac != null) {
            context = contextSupplier.get();
            context.setCacheInvokeConfig(cac);
        }
        if (context == null) {
            return method.invoke(src, args);
        } else {
            context.setInvoker(() -> method.invoke(src, args));
            context.setHiddenPackages(hiddenPackages);
            context.setArgs(args);
            context.setMethod(method);
            return invoke(context);
        }
    }

    public static Object invoke(CacheInvokeContext context) throws Throwable {
        if (context.getCacheInvokeConfig().isEnableCacheContext()) {
            try {
                CacheContextSupport._enable();
                return doInvoke(context);
            } finally {
                CacheContextSupport._disable();
            }
        } else {
            return doInvoke(context);
        }
    }

    private static Object doInvoke(CacheInvokeContext context) throws Throwable {
        CacheInvokeConfig cic = context.getCacheInvokeConfig();
        CachedAnnoConfig cachedConfig = cic.getCachedAnnoConfig();
        if (cachedConfig != null && (cachedConfig.isEnabled() || CacheContextSupport._isEnabled())) {
            return invokeWithCached(context);
        } else if (cic.getInvalidateAnnoConfig() != null || cic.getUpdateAnnoConfig() != null) {
            return invokeWithInvalidateOrUpdate(context);
        } else {
            return invokeOrigin(context);
        }
    }

    private static Object invokeWithInvalidateOrUpdate(CacheInvokeContext context) throws Throwable {
        Object originResult = invokeOrigin(context);
        context.setResult(originResult);
        CacheInvokeConfig cic = context.getCacheInvokeConfig();

        if (cic.getInvalidateAnnoConfig() != null) {
            Cache cache = context.getCacheFunction().apply(context, cic.getInvalidateAnnoConfig());
            boolean condition = ExpressionUtil.evalCondition(context, cic.getInvalidateAnnoConfig());
            if (cache != null && condition) {
                Object key = ExpressionUtil.evalKey(context, cic.getInvalidateAnnoConfig());
                if (key != null) {
                    cache.remove(key);
                }
            }
        }

        if (cic.getUpdateAnnoConfig() != null) {
            Cache cache = context.getCacheFunction().apply(context, cic.getUpdateAnnoConfig());
            boolean condition = ExpressionUtil.evalCondition(context, cic.getUpdateAnnoConfig());
            if (cache != null && condition) {
                Object key = ExpressionUtil.evalKey(context, cic.getUpdateAnnoConfig());
                Object value = ExpressionUtil.evalValue(context, cic.getUpdateAnnoConfig());
                if (key != null) {
                    cache.put(key, value);
                }
            }
        }

        return originResult;
    }

    private static Object invokeWithCached(CacheInvokeContext context)
            throws Throwable {
        CacheInvokeConfig cic = context.getCacheInvokeConfig();
        CachedAnnoConfig cac = cic.getCachedAnnoConfig();
        Cache cache = context.getCacheFunction().apply(context, cac);
        if (cache == null) {
            logger.error("no cache with name: " + context.getMethod());
            return invokeOrigin(context);
        }

        Object key = ExpressionUtil.evalKey(context, cic.getCachedAnnoConfig());
        if (key == null) {
            return loadAndCount(context, cache, key);
        }

        if (!ExpressionUtil.evalCondition(context, cic.getCachedAnnoConfig())) {
            return loadAndCount(context, cache, key);
        }

        try {
            Object result = cache.computeIfAbsent(key, (k) -> {
                try {
                    return invokeOrigin(context);
                } catch (Throwable e) {
                    throw new CacheInvokeException(e.getMessage(), e);
                }
            });
            if (cache instanceof CacheHandlerRefreshCache) {
                // We invoke addOrUpdateRefreshTask manually
                // because the cache has no loader(GET method will not invoke it)
                ((CacheHandlerRefreshCache) cache).addOrUpdateRefreshTask(key, (unusedKey) -> invokeOrigin(context));
            }
            return result;
        } catch (CacheInvokeException e) {
            throw e.getCause();
        }
    }

    private static Object loadAndCount(CacheInvokeContext context, Cache cache, Object key) throws Throwable {
        long t = System.currentTimeMillis();
        Object v = null;
        boolean success = false;
        try {
            v = invokeOrigin(context);
            success = true;
        } finally {
            t = System.currentTimeMillis() - t;
            CacheLoadEvent event = new CacheLoadEvent(cache, t, key, v, success);
            while(cache instanceof ProxyCache){
                cache = ((ProxyCache) cache).getTargetCache();
            }
            if (cache instanceof AbstractCache) {
                ((AbstractCache) cache).notify(event);
            }
        }
        return v;
    }

    private static Object invokeOrigin(CacheInvokeContext context) throws Throwable {
        return context.getInvoker().invoke();
    }

    public static class CacheHandlerRefreshCache<K, V> extends RefreshCache<K, V> {

        public CacheHandlerRefreshCache(Cache cache) {
            super(cache);
        }

        @Override
        public void addOrUpdateRefreshTask(K key, CacheLoader<K, V> loader) {
            super.addOrUpdateRefreshTask(key, loader);
        }
    }


}
