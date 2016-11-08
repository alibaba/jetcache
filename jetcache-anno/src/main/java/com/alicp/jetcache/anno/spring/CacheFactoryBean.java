/**
 * Created on  13-09-14 17:32
 */
package com.alicp.jetcache.anno.spring;

import com.alicp.jetcache.anno.impl.ProxyUtil;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheFactoryBean implements FactoryBean {

    private Object target;
    private GlobalCacheConfig globalCacheConfig;

    public Object getObject() throws Exception {
        return ProxyUtil.getProxyByAnnotation(target, globalCacheConfig);
    }

    public Class<?> getObjectType() {
        return null;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }
}
