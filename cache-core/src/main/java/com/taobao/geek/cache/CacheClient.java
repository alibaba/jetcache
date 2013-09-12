/**
 * Created on  13-09-04 14:03
 */
package com.taobao.geek.cache;

import com.taobao.geek.cache.impl.CacheImplSupport;

/**
 * @author yeli.hl
 */
public class CacheClient {

    private CacheFactory cacheFactory;

    public <T> T getProxy(T target, CacheConfig cacheConfig) {
        return CacheImplSupport.getProxy(target, cacheConfig, cacheFactory);
    }

    public <T> T getProxyByAnnotation(T target) {
        return CacheImplSupport.getProxyByAnnotation(target, cacheFactory);
    }

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }
}
