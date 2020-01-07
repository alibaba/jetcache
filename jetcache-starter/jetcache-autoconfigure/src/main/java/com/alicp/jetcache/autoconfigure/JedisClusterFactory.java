package com.alicp.jetcache.autoconfigure;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisCluster;

/**
 * jedis cluster instance factory, created on 2019/12/12
 *
 * @author <a href="mailto:eason.fengys@gmail.com">fengyingsheng</a>
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 *
 **/
public class JedisClusterFactory implements FactoryBean<JedisCluster> {

    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    private String key;

    private Class<?> jedisClusterClass;

    private boolean inited;

    private JedisCluster jedisCluster;

    public JedisClusterFactory(String key, Class<? extends JedisCluster> jedisClusterClass){
        this.key = key;
        this.jedisClusterClass = jedisClusterClass;
    }

    @Override
    public JedisCluster getObject() throws Exception {
        init();
        return jedisCluster;
    }

    private void init() {
        if (!inited) {
            jedisCluster = (JedisCluster) autoConfigureBeans.getCustomContainer().get("jedisCluster." + key);
            inited = true;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return jedisClusterClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
