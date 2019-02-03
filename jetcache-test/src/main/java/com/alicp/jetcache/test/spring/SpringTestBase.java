/**
 * Created on 2019/2/3.
 */
package com.alicp.jetcache.test.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class SpringTestBase implements ApplicationContextAware {
    protected ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
