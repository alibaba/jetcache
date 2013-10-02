/**
 * Created on  13-10-02 16:10
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class CacheInvokeContext {
    Invoker invoker;
    Object src;
    Method method;
    Object[] args;
    CacheProviderFactory cacheProviderFactory;
    CacheConfig cacheConfig;

    Invoker getInvoker() {
        return invoker;
    }

    void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    Object getSrc() {
        return src;
    }

    void setSrc(Object src) {
        this.src = src;
    }

    Method getMethod() {
        return method;
    }

    void setMethod(Method method) {
        this.method = method;
    }

    Object[] getArgs() {
        return args;
    }

    void setArgs(Object[] args) {
        this.args = args;
    }

    CacheProviderFactory getCacheProviderFactory() {
        return cacheProviderFactory;
    }

    void setCacheProviderFactory(CacheProviderFactory cacheProviderFactory) {
        this.cacheProviderFactory = cacheProviderFactory;
    }

    CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }
}
