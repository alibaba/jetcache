/**
 * Created on  13-10-07 23:27
 */
package com.alicp.jetcache.anno.spring;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.impl.CacheInvokeContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SpringGlobalCacheConfig extends GlobalCacheConfig implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    public SpringGlobalCacheConfig(){
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public CacheInvokeContext createCacheInvokeContext() {
        return new SpringCacheInvokeContext(applicationContext);
    }
}
