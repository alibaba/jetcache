/**
 * Created on  13-10-02 16:10
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInvokeContext {
    Invoker invoker;
    Method method;
    Object[] args;
    CacheInvokeConfig cacheInvokeConfig;

    Function<String, Cache> cacheFunction;
    String[] hiddenPackages;

    Object result = null;

    public CacheInvokeContext(){
    }


    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
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

    public void setCacheInvokeConfig(CacheInvokeConfig cacheInvokeConfig) {
        this.cacheInvokeConfig = cacheInvokeConfig;
    }

    public void setHiddenPackages(String[] hiddenPackages) {
        this.hiddenPackages = hiddenPackages;
    }

    public void setCacheFunction(Function<String, Cache> cacheFunction) {
        this.cacheFunction = cacheFunction;
    }

    //------------for eval script

    public Object[] getArgs() {
        return args;
    }

    public Object getResult() {
        return result;
    }
}
