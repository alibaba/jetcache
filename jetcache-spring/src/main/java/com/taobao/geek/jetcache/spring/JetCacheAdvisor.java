/**
 * Created on  13-09-19 20:40
 */
package com.taobao.geek.jetcache.spring;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * @author yeli.hl
 */
public class JetCacheAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private Pointcut pointcut = new JetCachePointcut();

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
}
