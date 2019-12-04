/**
 * Created on  13-09-09 15:59
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.support.*;
import com.alicp.jetcache.event.CacheLoadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
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
            super(null, null);
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

    @Override
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
        } else if (cic.getInvalidateAnnoConfigs() != null || cic.getUpdateAnnoConfig() != null) {
            return invokeWithInvalidateOrUpdate(context);
        } else {
            return invokeOrigin(context);
        }
    }

    private static Object invokeWithInvalidateOrUpdate(CacheInvokeContext context) throws Throwable {
        Object originResult = invokeOrigin(context);
        context.setResult(originResult);
        CacheInvokeConfig cic = context.getCacheInvokeConfig();

        if (cic.getInvalidateAnnoConfigs() != null) {
            doInvalidate(context, cic.getInvalidateAnnoConfigs());
        }
        CacheUpdateAnnoConfig updateAnnoConfig = cic.getUpdateAnnoConfig();
        if (updateAnnoConfig != null) {
            doUpdate(context, updateAnnoConfig);
        }

        return originResult;
    }

    private static Iterable toIterable(Object obj) {
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return Arrays.asList((Object[]) obj);
            } else {
                List list = new ArrayList();
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++) {
                    list.add(Array.get(obj, i));
                }
                return list;
            }
        } else if (obj instanceof Iterable) {
            return (Iterable) obj;
        } else {
            return null;
        }
    }

    private static void doInvalidate(CacheInvokeContext context, List<CacheInvalidateAnnoConfig> annoConfig) {
        for (CacheInvalidateAnnoConfig config : annoConfig) {
            doInvalidate(context, config);
        }
    }

    private static void doInvalidate(CacheInvokeContext context, CacheInvalidateAnnoConfig annoConfig) {
        Cache cache = context.getCacheFunction().apply(context, annoConfig);
        if (cache == null) {
            return;
        }
        boolean condition = ExpressionUtil.evalCondition(context, annoConfig);
        if (!condition) {
            return;
        }
        Object key = ExpressionUtil.evalKey(context, annoConfig);
        if (key == null) {
            return;
        }
        if (annoConfig.isMulti()) {
            Iterable it = toIterable(key);
            if (it == null) {
                logger.error("jetcache @CacheInvalidate key is not instance of Iterable or array: " + annoConfig.getDefineMethod());
                return;
            }
            Set keys = new HashSet();
            it.forEach(k -> keys.add(k));
            cache.removeAll(keys);
        } else {
            cache.remove(key);
        }
    }

    private static void doUpdate(CacheInvokeContext context, CacheUpdateAnnoConfig updateAnnoConfig) {
        Cache cache = context.getCacheFunction().apply(context, updateAnnoConfig);
        if (cache == null) {
            return;
        }
        boolean condition = ExpressionUtil.evalCondition(context, updateAnnoConfig);
        if (!condition) {
            return;
        }
        Object key = ExpressionUtil.evalKey(context, updateAnnoConfig);
        Object value = ExpressionUtil.evalValue(context, updateAnnoConfig);
        if (key == null || value == ExpressionUtil.EVAL_FAILED) {
            return;
        }
        if (updateAnnoConfig.isMulti()) {
            if (value == null) {
                return;
            }
            Iterable keyIt = toIterable(key);
            Iterable valueIt = toIterable(value);
            if (keyIt == null) {
                logger.error("jetcache @CacheUpdate key is not instance of Iterable or array: " + updateAnnoConfig.getDefineMethod());
                return;
            }
            if (valueIt == null) {
                logger.error("jetcache @CacheUpdate value is not instance of Iterable or array: " + updateAnnoConfig.getDefineMethod());
                return;
            }

            List keyList = new ArrayList();
            List valueList = new ArrayList();
            keyIt.forEach(o -> keyList.add(o));
            valueIt.forEach(o -> valueList.add(o));
            if (keyList.size() != valueList.size()) {
                logger.error("jetcache @CacheUpdate key size not equals with value size: " + updateAnnoConfig.getDefineMethod());
                return;
            } else {
                Map m = new HashMap();
                for (int i = 0; i < valueList.size(); i++) {
                    m.put(keyList.get(i), valueList.get(i));
                }
                cache.putAll(m);
            }
        } else {
            cache.put(key, value);
        }
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
            CacheLoader loader = new CacheLoader() {
                @Override
                public Object load(Object k) throws Throwable {
                    Object result = invokeOrigin(context);
                    context.setResult(result);
                    return result;
                }

                @Override
                public boolean vetoCacheUpdate() {
                    return !ExpressionUtil.evalPostCondition(context, cic.getCachedAnnoConfig());
                }
            };
            Object result = cache.computeIfAbsent(key, loader);
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
            while (cache instanceof ProxyCache) {
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
