/**
 * Created on  13-09-04 15:34
 */
package com.taobao.geek.jetcache;

import com.taobao.geek.jetcache.impl.CacheImplSupport;

/**
 * @author yeli.hl
 */
public class CacheContext {

    private CacheContext() {
    }

    public static <T> T enableCache(final T target) {
        return CacheImplSupport.enableCache(target);
    }

    public static void enableCache(Callback callback) throws CallbackException{
        CacheImplSupport.enableCache(callback);
    }

    public static <T> T enableCache(ReturnValueCallback<T> callback) throws CallbackException {
        return CacheImplSupport.enableCache(callback);
    }

}
