/**
 * Created on  13-10-07 23:25
 */
package com.alicp.jetcache.anno.spring;

import com.alicp.jetcache.anno.impl.CacheInvokeContext;
import org.springframework.context.ApplicationContext;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SpringCacheInvokeContext extends CacheInvokeContext {
    protected ApplicationContext context;

    public SpringCacheInvokeContext(ApplicationContext context) {
        this.context = context;
    }

    public Object bean(String name) {
        return context.getBean(name);
    }
}
