/**
 * Created on  13-10-02 16:10
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheInvokeContext {
    Invoker invoker;
    Method method;
    Object[] args;
    CacheInvokeConfig cacheInvokeConfig;

    Function<CacheInvokeContext, Cache> cacheFunction;
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

    public CacheInvokeConfig getCacheInvokeConfig() {
        return cacheInvokeConfig;
    }

    public void setHiddenPackages(String[] hiddenPackages) {
        this.hiddenPackages = hiddenPackages;
    }

    public String[] getHiddenPackages() {
        return hiddenPackages;
    }

    public void setCacheFunction(Function<CacheInvokeContext, Cache> cacheFunction) {
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
