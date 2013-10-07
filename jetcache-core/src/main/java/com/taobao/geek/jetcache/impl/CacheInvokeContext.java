/**
 * Created on  13-10-02 16:10
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.GlobalCacheConfig;
import com.taobao.geek.jetcache.CacheResultCode;

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

    //status, without getter and setter
    boolean needUpdateLocal = false;
    boolean needUpdateRemote = false;
    CacheResultCode localResult = null;
    CacheResultCode remoteResult = null;

    public CacheInvokeContext(){
    }


    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Object getTarget() {
        return target;
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

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public GlobalCacheConfig getGlobalCacheConfig() {
        return globalCacheConfig;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public CacheInvokeConfig getCacheInvokeConfig() {
        return cacheInvokeConfig;
    }

    public void setCacheInvokeConfig(CacheInvokeConfig cacheInvokeConfig) {
        this.cacheInvokeConfig = cacheInvokeConfig;
    }
}
