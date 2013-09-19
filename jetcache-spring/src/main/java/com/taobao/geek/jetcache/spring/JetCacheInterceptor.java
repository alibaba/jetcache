/**
 * Created on  13-09-18 20:33
 */
package com.taobao.geek.jetcache.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author yeli.hl
 */
public class JetCacheInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }
}
