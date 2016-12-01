package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.method.SpringCacheContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created on 2016/12/1.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SpringConfigProvider extends ConfigProvider implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public CacheContext newContext(GlobalCacheConfig globalCacheConfig) {
        return new SpringCacheContext(globalCacheConfig, applicationContext);
    }
}
