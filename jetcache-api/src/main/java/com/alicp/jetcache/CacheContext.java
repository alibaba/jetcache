/**
 * Created on  13-09-04 15:34
 */
package com.alicp.jetcache;

import com.alicp.jetcache.anno.EnableCache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheContext {

    private static ThreadLocal<CacheThreadLocal> cacheThreadLocal = new ThreadLocal<CacheThreadLocal>() {
        @Override
        protected CacheThreadLocal initialValue() {
            return new CacheThreadLocal();
        }
    };

    protected CacheContext() {
    }

    /**
     * Enable cache in current thread, for @Cached(enabled=false).
     *
     * @param callback
     * @throws CallbackException If the callback throws throws an exception
     * @see EnableCache
     */
    public static void enableCache(Callback callback) throws CallbackException {
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

    /**
     * Enable cache in current thread, for @Cached(enabled=false).
     * Notice the return value of callback is not cached.
     * @return
     * @param callback
     * @throws CallbackException If the callback throws throws an exception
     * @see EnableCache
     */
    public static <T> T enableCache(ReturnValueCallback<T> callback) throws CallbackException {
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

    protected static void enable(){
        CacheThreadLocal var = cacheThreadLocal.get();
        var.setEnabledCount(var.getEnabledCount() + 1);
    }

    protected static void disable(){
        CacheThreadLocal var = cacheThreadLocal.get();
        var.setEnabledCount(var.getEnabledCount() - 1);
    }

    protected static boolean isEnabled() {
        return cacheThreadLocal.get().getEnabledCount() > 0;
    }

}
