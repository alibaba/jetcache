/**
 * Created on  13-09-19 20:40
 */
package com.alicp.jetcache.anno.spring;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private CachePointcut pointcut;

    private ConcurrentHashMap cacheConfigMap;

    private String[] basePackages;

    public Pointcut getPointcut() {
        pointcut = new CachePointcut(basePackages);
        pointcut.setCacheConfigMap(cacheConfigMap);
        return pointcut;
    }

    public void setCacheConfigMap(ConcurrentHashMap cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }
}
