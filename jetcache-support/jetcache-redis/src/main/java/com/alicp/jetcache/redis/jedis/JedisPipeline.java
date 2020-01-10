package com.alicp.jetcache.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * Created on 2019/12/12.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public class JedisPipeline extends Pipeline implements AbstractJedisPipeline {

    private Jedis jedis;

    public JedisPipeline(final Jedis jedis) {
        this.jedis = jedis;
        super.setClient(jedis.getClient());
    }

    @Override
    public void close() {
        super.close();
        jedis.close();
    }
}
