/**
 * Created on  13-09-04 14:03
 */
package com.taobao.geek.cache;

import com.taobao.geek.cache.impl.CacheImplSupport;

/**
 * @author yeli.hl
 */
public class CacheClient {

    private CacheProviderFactory cacheProviderFactory;

    public <T> T getProxy(T target, CacheConfig cacheConfig) {
        return CacheImplSupport.getProxy(target, cacheConfig, cacheProviderFactory);
    }

    public <T> T getProxyByAnnotation(T target) {
        return CacheImplSupport.getProxyByAnnotation(target, cacheProviderFactory);
    }

    public void setCacheProviderFactory(CacheProviderFactory cacheProviderFactory) {
        this.cacheProviderFactory = cacheProviderFactory;
    }

    public CacheProviderFactory getCacheProviderFactory() {
        return cacheProviderFactory;
    }
}
