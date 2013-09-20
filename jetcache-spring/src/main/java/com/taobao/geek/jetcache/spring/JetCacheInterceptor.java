/**
 * Created on  13-09-18 20:33
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author yeli.hl
 */
public class JetCacheInterceptor implements MethodInterceptor {

    private IdentityHashMap cacheConfigMap;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }

    public void setCacheConfigMap(IdentityHashMap cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }
}
