package com.alicp.jetcache.anno.config.combined;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created on 2017/2/15.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Aspect
public class AspectJAspect {
    @Before("(target(com.alicp.jetcache.anno.config.combined.ServiceImpl) && execution(* *()))")
    public void after(JoinPoint jointPoint) {
        System.out.println("before " + jointPoint.getSignature());
    }
}
