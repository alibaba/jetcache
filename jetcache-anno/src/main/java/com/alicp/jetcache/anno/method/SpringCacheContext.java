package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import org.springframework.context.ApplicationContext;

/**
 * Created on 2016/10/19.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringCacheContext extends CacheContext {

    private ApplicationContext applicationContext;

    public SpringCacheContext(CacheManager cacheManager, SpringConfigProvider configProvider,
                              GlobalCacheConfig globalCacheConfig, ApplicationContext applicationContext) {
        super(cacheManager, configProvider, globalCacheConfig);
        this.applicationContext = applicationContext;
    }

    @Override
    protected CacheInvokeContext newCacheInvokeContext() {
        return new SpringCacheInvokeContext(applicationContext);
    }

}
