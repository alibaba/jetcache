/**
 * Created on  13-09-18 20:33
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.*;
import com.taobao.geek.jetcache.impl.CacheInvokeConfig;
import com.taobao.geek.jetcache.impl.CacheImplSupport;
import com.taobao.geek.jetcache.impl.Invoker;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInterceptor implements MethodInterceptor {

    private IdentityHashMap<Method, CacheInvokeConfig> cacheConfigMap;
    private CacheProviderFactory cacheProviderFactory;

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
        return CacheImplSupport.invoke(invoker, invocation.getThis(), invocation.getMethod(),
                invocation.getArguments(), cacheProviderFactory, cac);
    }

    public void setCacheConfigMap(IdentityHashMap<Method, CacheInvokeConfig> cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }

    public void setCacheProviderFactory(CacheProviderFactory cacheProviderFactory) {
        this.cacheProviderFactory = cacheProviderFactory;
    }
}
