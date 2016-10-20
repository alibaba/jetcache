/**
 * Created on  13-09-18 20:33
 */
package com.alicp.jetcache.anno.spring;

import com.alicp.jetcache.anno.impl.CacheHandler;
import com.alicp.jetcache.anno.impl.CacheInvokeConfig;
import com.alicp.jetcache.anno.impl.CacheInvokeContext;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInterceptor implements MethodInterceptor {

    private ConcurrentHashMap<Method, CacheInvokeConfig> cacheConfigMap;
    private GlobalCacheConfig globalCacheConfig;

    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final CacheInvokeConfig cac = cacheConfigMap.get(invocation.getMethod());
        if (cac == null) {
            return invocation.proceed();
        }

        CacheInvokeContext context = globalCacheConfig.cacheContext().createCacheInvokeContext();
        context.setInvoker(() -> invocation.proceed());
        context.setMethod(invocation.getMethod());
        context.setArgs(invocation.getArguments());
        context.setCacheInvokeConfig(cac);
        context.setHiddenPackages(globalCacheConfig.getHidePackages());
        return CacheHandler.invoke(context);
    }

    public void setCacheConfigMap(ConcurrentHashMap<Method, CacheInvokeConfig> cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }
}
