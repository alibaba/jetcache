package com.alicp.jetcache.anno.config.combined;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;

/**
 * Created on 2017/2/15.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Aspect
public class ServiceMonitorAspect {
    public void monitor(JoinPoint jointPoint) throws Throwable {
        System.out.println("monitor before " + jointPoint.getSignature());
    }
}
