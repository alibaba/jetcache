/**
 * Created on  13-09-09 15:59
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.ProxyCache;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.event.CacheLoadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheHandler implements InvocationHandler {
    private static Logger logger = LoggerFactory.getLogger(CacheHandler.class);

    private Object src;
    private Supplier<CacheInvokeContext> contextSupplier;
    private String[] hiddenPackages;
    private CacheInvokeConfig cacheInvokeConfig;
    private HashMap<String, CacheInvokeConfig> configMap;

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

    public CacheHandler(Object src, CacheInvokeConfig cacheInvokeConfig, Supplier<CacheInvokeContext> contextSupplier, String[] hiddenPackages) {
        this.src = src;
        this.cacheInvokeConfig = cacheInvokeConfig;
        this.contextSupplier = contextSupplier;
        this.hiddenPackages = hiddenPackages;
    }

    public CacheHandler(Object src, HashMap<String, CacheInvokeConfig> configMap, Supplier<CacheInvokeContext> contextSupplier, String[] hiddenPackages) {
        this.src = src;
        this.configMap = configMap;
        this.contextSupplier = contextSupplier;
        this.hiddenPackages = hiddenPackages;
    }

    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        CacheInvokeContext context = null;
        if (cacheInvokeConfig != null) {
            context = contextSupplier.get();
            context.cacheInvokeConfig = cacheInvokeConfig;
        } else {
            String sig = ClassUtil.getMethodSig(method);
            CacheInvokeConfig cac = configMap.get(sig);
            if (cac != null) {
                context = contextSupplier.get();
                context.cacheInvokeConfig = cac;
            }
        }
        if (context == null) {
            return method.invoke(src, args);
        } else {
            context.invoker = () -> method.invoke(src, args);
            context.hiddenPackages = hiddenPackages;
            context.args = args;
            context.method = method;
            return invoke(context);
        }
    }

    public static Object invoke(CacheInvokeContext context) throws Throwable {
        if (context.cacheInvokeConfig.isEnableCacheContext()) {
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
        CacheAnnoConfig cacheAnnoConfig = context.cacheInvokeConfig.getCacheAnnoConfig();
        if (cacheAnnoConfig != null && (cacheAnnoConfig.isEnabled() || CacheContextSupport._isEnabled())) {
            return invokeWithCache(context);
        } else {
            return invokeOrigin(context);
        }
    }

    private static Object invokeWithCache(CacheInvokeContext context)
            throws Throwable {

        Cache cache = context.cacheFunction.apply(context);
        if (cache == null) {
            logger.error("no cache with name: " + context.method);
            return invokeOrigin(context);
        }

        Object key = context.args;
        if (key == null) {
            key = "_$JETCACHE_NULL_KEY$_";
        }

        if (!ExpressionUtil.evalCondition(context)) {
            return loadAndCount(context, cache, key);
        }

        // the semantics of "unless" and "cacheNullValue" is not very accurate, we do our best to process it.
        CacheGetResult cacheGetResult = cache.GET(key);
        if (cacheGetResult.isSuccess()) {
            context.result = cacheGetResult.getValue();
        }
        if (!cacheGetResult.isSuccess()) {//not hit
            context.result = loadAndCount(context, cache, key);
            if (!canNotCache(context)) {
                cache.put(key, context.result);
            }
        } else { //cache hit
            if (canNotCache(context)) {
                context.result = loadAndCount(context, cache, key);//reload
                if (!canNotCache(context)) {//eval again
                    cache.put(key, context.result);
                }
            }
        }

        return context.result;
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

    private static boolean canNotCache(CacheInvokeContext context) {
        return ExpressionUtil.evalUnless(context) ||
                (context.result == null && !context.cacheInvokeConfig.getCacheAnnoConfig().isCacheNullValue());
    }

    private static Object invokeOrigin(CacheInvokeContext context) throws Throwable {
        return context.invoker.invoke();
    }


}
