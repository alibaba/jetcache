package com.alicp.jetcache.autoconfigure;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.UnifiedJedis;

/**
 * Created on 2016/12/28.
 *
 * @author huangli
 */
public class JedisFactory implements FactoryBean<UnifiedJedis> {
    private String key;
    private Class<?> poolClass;

    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    private boolean inited;
    private UnifiedJedis unifiedJedis;

    public JedisFactory(String key, Class<? extends UnifiedJedis> poolClass){
        this.key = key;
        this.poolClass = poolClass;
    }

    public String getKey() {
        return key;
    }

    @Override
    public UnifiedJedis getObject() throws Exception {
        if (!inited) {
            unifiedJedis = (UnifiedJedis) autoConfigureBeans.getCustomContainer().get("jedis." + key);
            inited = true;
        }
        return unifiedJedis;
    }

    @Override
    public Class<?> getObjectType() {
        return poolClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
