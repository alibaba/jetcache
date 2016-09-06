/**
 * Created on  13-09-04 15:34
 */
package com.alicp.jetcache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheContext {

    private static CacheContextSupport support = new CacheContextSupport();

    private CacheContext() {
    }

    /**
     * Enable cache in current thread, for @Cached(enabled=false).
     *
     * @param callback
     * @throws CallbackException If the callback throws throws an exception
     * @see EnableCache
     */
    public static void enableCache(Callback callback) throws CallbackException {
        support.enableCache(callback);
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
        return support.enableCache(callback);
    }

}
