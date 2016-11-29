package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.method.SpringCacheInvokeContext;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.method.CacheInvokeContext;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.support.DefaultCacheMonitorManager;
import org.springframework.context.ApplicationContext;

import java.util.function.Consumer;

/**
 * Created on 2016/10/19.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SpringCacheContext extends CacheContext {

    private ApplicationContext applicationContext;

    public SpringCacheContext(GlobalCacheConfig globalCacheConfig, ApplicationContext applicationContext,
                              int statIntervalMinutes, Consumer<DefaultCacheMonitorManager.StatInfo> statCallback) {
        super(globalCacheConfig, statIntervalMinutes, statCallback);
        this.applicationContext = applicationContext;
    }

    @Override
    protected CacheInvokeContext newCacheInvokeContext() {
        return new SpringCacheInvokeContext(applicationContext);
    }
}
