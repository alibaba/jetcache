/**
 * Created on  13-09-04 15:34
 */
package com.taobao.geek.jetcache;

import com.taobao.geek.jetcache.Callback;
import com.taobao.geek.jetcache.CallbackException;
import com.taobao.geek.jetcache.ReturnValueCallback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheContextSupport {

    private static ThreadLocal<CacheThreadLocal> cacheThreadLocal = new ThreadLocal<CacheThreadLocal>() {
        @Override
        protected CacheThreadLocal initialValue() {
            return new CacheThreadLocal();
        }
    };

    public CacheContextSupport() {
    }

    protected void enableCache(Callback callback) throws CallbackException {
        CacheThreadLocal var = cacheThreadLocal.get();
        try {
            var.setEnabledCount(var.getEnabledCount() + 1);
            callback.execute();
        } catch (Throwable e) {
            throw new CallbackException(e);
        } finally {
            var.setEnabledCount(var.getEnabledCount() - 1);
        }
    }

    protected <T> T enableCache(ReturnValueCallback<T> callback) throws CallbackException{
        CacheThreadLocal var = cacheThreadLocal.get();
        try {
            var.setEnabledCount(var.getEnabledCount() + 1);
            return callback.execute();
        } catch (Throwable e) {
            throw new CallbackException(e);
        } finally {
            var.setEnabledCount(var.getEnabledCount() - 1);
        }
    }

    protected void enable(){
        CacheThreadLocal var = cacheThreadLocal.get();
        var.setEnabledCount(var.getEnabledCount() + 1);
    }

    protected void disable(){
        CacheThreadLocal var = cacheThreadLocal.get();
        var.setEnabledCount(var.getEnabledCount() - 1);
    }

    protected boolean isEnabled() {
        return cacheThreadLocal.get().getEnabledCount() > 0;
    }

}
