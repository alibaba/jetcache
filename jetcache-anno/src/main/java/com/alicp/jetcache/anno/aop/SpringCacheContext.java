package com.alicp.jetcache.anno.aop;

import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.method.CacheInvokeContext;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.context.ApplicationContext;

/**
 * Created on 2016/10/19.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SpringCacheContext extends CacheContext {

    private ApplicationContext applicationContext;

    public SpringCacheContext(GlobalCacheConfig globalCacheConfig, ApplicationContext applicationContext) {
        super(globalCacheConfig);
        this.applicationContext = applicationContext;
    }

    @Override
    protected CacheInvokeContext newCacheInvokeContext() {
        return new SpringCacheInvokeContext(applicationContext);
    }
}
