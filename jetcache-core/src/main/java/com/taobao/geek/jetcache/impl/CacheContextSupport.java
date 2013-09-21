/**
 * Created on  13-09-04 15:34
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.Callback;
import com.taobao.geek.jetcache.ReturnValueCallback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author yeli.hl
 */
class CacheContextSupport {

    private static ThreadLocal<CacheThreadLocal> cacheThreadLocal = new ThreadLocal<CacheThreadLocal>() {
        @Override
        protected CacheThreadLocal initialValue() {
            return new CacheThreadLocal();
        }
    };

    private CacheContextSupport() {
    }

    public static <T> T enableCache(final T target) {
        Class<?>[] its = ClassUtil.getAllInterfaces(target);
        Object o = Proxy.newProxyInstance(target.getClass().getClassLoader(), its, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                CacheThreadLocal var = cacheThreadLocal.get();
                try {
                    var.setEnabledCount(var.getEnabledCount() + 1);
                    return method.invoke(target, args);
                } finally {
                    var.setEnabledCount(var.getEnabledCount() - 1);
                }
            }
        });
        return (T) o;
    }

    public static void enableCache(Callback callback) throws Exception{
        CacheThreadLocal var = cacheThreadLocal.get();
        try {
            var.setEnabledCount(var.getEnabledCount() + 1);
            callback.execute();
        } finally {
            var.setEnabledCount(var.getEnabledCount() - 1);
        }
    }

    public static <T> T enableCache(ReturnValueCallback<T> callback) throws Exception{
        CacheThreadLocal var = cacheThreadLocal.get();
        try {
            var.setEnabledCount(var.getEnabledCount() + 1);
            return callback.execute();
        } finally {
            var.setEnabledCount(var.getEnabledCount() - 1);
        }
    }

    public static boolean isEnabled() {
        return cacheThreadLocal.get().getEnabledCount() > 0;
    }

}
