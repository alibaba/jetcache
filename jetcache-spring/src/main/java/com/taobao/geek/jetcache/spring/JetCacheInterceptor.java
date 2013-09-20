/**
 * Created on  13-09-18 20:33
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;
import com.taobao.geek.jetcache.impl.CacheImplSupport;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public class JetCacheInterceptor implements MethodInterceptor {

    private IdentityHashMap<Method, CacheConfig> cacheConfigMap;
    private CacheProviderFactory cacheProviderFactory;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        CacheConfig cc = cacheConfigMap.get(invocation.getMethod());
        if (cc != null) {
            return CacheImplSupport.invoke(invocation.getThis(), invocation.getMethod(),
                    invocation.getArguments(), cacheProviderFactory, cc);
        } else {
            return invocation.proceed();
        }
    }

    public void setCacheConfigMap(IdentityHashMap<Method, CacheConfig> cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }

    public void setCacheProviderFactory(CacheProviderFactory cacheProviderFactory) {
        this.cacheProviderFactory = cacheProviderFactory;
    }
}
