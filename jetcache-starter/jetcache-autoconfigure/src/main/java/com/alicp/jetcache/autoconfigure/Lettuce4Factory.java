package com.alicp.jetcache.autoconfigure;

import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.api.StatefulConnection;
import com.lambdaworks.redis.cluster.api.async.RedisClusterAsyncCommands;
import com.lambdaworks.redis.cluster.api.rx.RedisClusterReactiveCommands;
import com.lambdaworks.redis.cluster.api.sync.RedisClusterCommands;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created on 2017/5/11.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class Lettuce4Factory implements FactoryBean {
    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    private boolean inited;
    private Object obj;
    private Class<?> clazz;
    private String key;

    // for unit test
    Lettuce4Factory(AutoConfigureBeans autoConfigureBeans, String key, Class<?> clazz) {
        this(key, clazz);
        this.autoConfigureBeans = autoConfigureBeans;
    }


    public Lettuce4Factory(String key, Class<?> clazz) {
        this.clazz = clazz;
        if (AbstractRedisClient.class.isAssignableFrom(clazz)) {
            key += ".client";
        } else if (StatefulConnection.class.isAssignableFrom(clazz)) {
            key += ".connection";
        } else if (RedisClusterCommands.class.isAssignableFrom(clazz)) {
            // RedisCommands extends RedisClusterCommands
            key += ".commands";
        } else if (RedisClusterAsyncCommands.class.isAssignableFrom(clazz)) {
            // RedisAsyncCommands extends RedisClusterAsyncCommands
            key += ".asyncCommands";
        } else if (RedisClusterReactiveCommands.class.isAssignableFrom(clazz)) {
            // RedisReactiveCommands extends RedisClusterReactiveCommands
            key += ".reactiveCommands";
        } else {
            throw new IllegalArgumentException(clazz.getName());
        }
        this.key = key;
    }

    private void init() {
        if (!inited) {
            obj = autoConfigureBeans.getCustomContainer().get(key);
            inited = true;
        }
    }

    @Override
    public Object getObject() throws Exception {
        init();
        return obj;
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
