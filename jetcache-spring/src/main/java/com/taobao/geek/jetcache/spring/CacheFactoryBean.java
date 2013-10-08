/**
 * Created on  13-09-14 17:32
 */
package com.taobao.geek.jetcache.spring;

import com.taobao.geek.jetcache.support.CacheClient;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheFactoryBean implements FactoryBean {

    private Object target;
    private CacheClient cacheClient;

    @Override
    public Object getObject() throws Exception {
        return cacheClient.getProxyByAnnotation(target);
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public CacheClient getCacheClient() {
        return cacheClient;
    }

    public void setCacheClient(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }
}
