/**
 * Created on  13-10-02 16:10
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInvokeContext {
    Invoker invoker;
    Object target;
    Method method;
    Object[] args;
    GlobalCacheConfig globalCacheConfig;
    CacheInvokeConfig cacheInvokeConfig;

    Object result = null;
    Throwable exception = null;

    public CacheInvokeContext(){
    }


    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }

    public void setCacheInvokeConfig(CacheInvokeConfig cacheInvokeConfig) {
        this.cacheInvokeConfig = cacheInvokeConfig;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getResult() {
        return result;
    }
}
