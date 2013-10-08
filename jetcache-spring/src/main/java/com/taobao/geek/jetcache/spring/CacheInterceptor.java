/**
 * Created on  13-09-18 20:33
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.GlobalCacheConfig;
import com.taobao.geek.jetcache.impl.CacheImplSupport;
import com.taobao.geek.jetcache.impl.CacheInvokeConfig;
import com.taobao.geek.jetcache.impl.CacheInvokeContext;
import com.taobao.geek.jetcache.impl.Invoker;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInterceptor implements MethodInterceptor {

    private IdentityHashMap<Method, CacheInvokeConfig> cacheConfigMap;
    private GlobalCacheConfig globalCacheConfig;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final CacheInvokeConfig cac = cacheConfigMap.get(invocation.getMethod());
        if (cac == null) {
            return invocation.proceed();
        }
        Invoker invoker = new Invoker() {
            @Override
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
        return CacheImplSupport.invoke(context);
    }

    public void setCacheConfigMap(IdentityHashMap<Method, CacheInvokeConfig> cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }
}
