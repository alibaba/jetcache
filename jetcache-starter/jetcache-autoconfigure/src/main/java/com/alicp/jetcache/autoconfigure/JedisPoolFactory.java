package com.alicp.jetcache.autoconfigure;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisPool;

/**
 * Created on 2016/12/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class JedisPoolFactory implements FactoryBean<JedisPool> {
    private String key;

    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    private boolean inited;
    private JedisPool jedisPool;

    public JedisPoolFactory(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public JedisPool getObject() throws Exception {
        if (!inited) {
            jedisPool = (JedisPool) autoConfigureBeans.getCustomContainer().get("jedisPool." + key);
            inited = true;
        }
        return jedisPool;
    }

    @Override
    public Class<?> getObjectType() {
        return JedisPool.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
