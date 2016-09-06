/**
 * Created on  13-10-28 23:42
 */
package com.alicp.jetcache.spring.beans;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MyFactoryBean implements FactoryBean,InitializingBean {

    private FactoryBeanTarget target;

    @Override
    public Object getObject() throws Exception {
        return target;
    }

    @Override
    public Class<?> getObjectType() {
        return FactoryBeanTarget.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final FactoryBeanTarget SRC = new FactoryBeanTargetImpl();
        target = (FactoryBeanTarget) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{FactoryBeanTarget.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(SRC, args);
                    }
                });
    }
}
