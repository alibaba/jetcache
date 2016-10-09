/**
 * Created on  13-09-18 20:33
 */
package com.alicp.jetcache.anno.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.alicp.jetcache.anno.impl.CacheHandler;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.impl.CacheInvokeConfig;
import com.alicp.jetcache.anno.impl.CacheInvokeContext;
import com.alicp.jetcache.anno.impl.Invoker;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInterceptor implements MethodInterceptor {

    private IdentityHashMap<Method, CacheInvokeConfig> cacheConfigMap;
    private GlobalCacheConfig globalCacheConfig;

    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final CacheInvokeConfig cac = cacheConfigMap.get(invocation.getMethod());
        if (cac == null) {
            return invocation.proceed();
        }
        Invoker invoker = new Invoker() {
            public Object invoke() throws Throwable {
                return invocation.proceed();
            }
        };

        CacheInvokeContext context = globalCacheConfig.createCacheInvokeContext();
        context.setInvoker(invoker);
        context.setTarget(invocation.getThis());
        context.setMethod(invocation.getMethod());
        context.setArgs(invocation.getArguments());
        context.setGlobalCacheConfig(globalCacheConfig);
        context.setCacheInvokeConfig(cac);
        return CacheHandler.invoke(context);
    }

    public void setCacheConfigMap(IdentityHashMap<Method, CacheInvokeConfig> cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }
}
