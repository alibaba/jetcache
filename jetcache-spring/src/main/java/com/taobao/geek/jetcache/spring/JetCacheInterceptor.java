/**
 * Created on  13-09-18 20:33
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.CacheConfig;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
public class JetCacheInterceptor implements MethodInterceptor {

    private IdentityHashMap<Method, CacheConfig> cacheConfigMap;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (cacheConfigMap.get(invocation.getMethod()) != null) {
            System.out.println("cached:" + invocation);
        }
        System.out.println(invocation);
        return invocation.proceed();
    }

    public void setCacheConfigMap(IdentityHashMap<Method, CacheConfig> cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }
}
