/**
 * Created on  13-09-04 15:34
 */
package com.taobao.geek.cache;

/**
 * @author yeli.hl
 */
public class CacheContext {

    private static ThreadLocal<CacheThreadLocal> cacheThreadLocal = new ThreadLocal<CacheThreadLocal>() {
        @Override
        protected CacheThreadLocal initialValue() {
            return new CacheThreadLocal();
        }
    };

    private CacheContext() {
    }

    public static <T> T enableCache(T target) {
        return null;  //TODO
    }

    static boolean isCacheEnabled() {
        return cacheThreadLocal.get().isEnabled();
    }

}
