/**
 * Created on  13-09-18 20:33
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.*;
import com.taobao.geek.jetcache.impl.CacheAnnoConfig;
import com.taobao.geek.jetcache.impl.CacheImplSupport;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public class JetCacheInterceptor implements MethodInterceptor {

    private IdentityHashMap<Method, CacheAnnoConfig> cacheConfigMap;
    private CacheProviderFactory cacheProviderFactory;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final CacheAnnoConfig cc = cacheConfigMap.get(invocation.getMethod());
        if (cc == null) {
            return invocation.proceed();
        }
        if (cc.isEnableCacheContext()) {
            return CacheContext.enableCache(new ReturnValueCallback<Object>() {
                @Override
                public Object execute() throws Exception {
                    try {
                        return invoke(invocation, cc);
                    } catch (Throwable e) {
                        if (e instanceof Exception) {
                            throw (Exception) e;
                        } else if (e instanceof Error) {
                            throw (Error) e;
                        } else {
                            throw new CacheException("", e);
                        }
                    }
                }
            });
        } else {
            return invoke(invocation, cc);
        }

    }

    private Object invoke(MethodInvocation invocation, CacheAnnoConfig cc) throws Throwable {
        if (cc.getCacheConfig() != null) {
            return CacheImplSupport.invoke(invocation.getThis(), invocation.getMethod(),
                    invocation.getArguments(), cacheProviderFactory, cc.getCacheConfig());
        } else {
            return invocation.proceed();
        }
    }

    public void setCacheConfigMap(IdentityHashMap<Method, CacheAnnoConfig> cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }

    public void setCacheProviderFactory(CacheProviderFactory cacheProviderFactory) {
        this.cacheProviderFactory = cacheProviderFactory;
    }
}
