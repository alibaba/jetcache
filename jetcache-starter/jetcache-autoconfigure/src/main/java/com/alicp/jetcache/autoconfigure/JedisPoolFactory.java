package com.alicp.jetcache.autoconfigure;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 * Created on 2016/12/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JedisPoolFactory implements FactoryBean<Pool<Jedis>> {
    private String key;
    private Class<?> poolClass;

    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    private boolean inited;
    private Pool<Jedis> jedisPool;

    public JedisPoolFactory(String key, Class<? extends Pool<Jedis>> poolClass){
        this.key = key;
        this.poolClass = poolClass;
    }

    public String getKey() {
        return key;
    }

    @Override
    public Pool<Jedis> getObject() throws Exception {
        if (!inited) {
            jedisPool = (Pool<Jedis>) autoConfigureBeans.getCustomContainer().get("jedisPool." + key);
            inited = true;
        }
        return jedisPool;
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
