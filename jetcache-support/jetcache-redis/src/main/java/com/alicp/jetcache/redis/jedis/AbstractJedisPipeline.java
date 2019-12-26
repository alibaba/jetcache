package com.alicp.jetcache.redis.jedis;

import redis.clients.jedis.commands.BasicRedisPipeline;
import redis.clients.jedis.commands.BinaryRedisPipeline;
import redis.clients.jedis.commands.BinaryScriptingCommandsPipeline;
import redis.clients.jedis.commands.ClusterPipeline;
import redis.clients.jedis.commands.MultiKeyBinaryRedisPipeline;
import redis.clients.jedis.commands.MultiKeyCommandsPipeline;
import redis.clients.jedis.commands.RedisPipeline;
import redis.clients.jedis.commands.ScriptingCommandsPipeline;

import java.io.Closeable;
import java.util.List;

/**
 * Created on 2019/12/12.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public interface AbstractJedisPipeline
        extends MultiKeyBinaryRedisPipeline, MultiKeyCommandsPipeline, ClusterPipeline, BinaryScriptingCommandsPipeline,
        ScriptingCommandsPipeline, BasicRedisPipeline, BinaryRedisPipeline, RedisPipeline, Closeable {

    /**
     * Synchronize pipeline by reading all responses.
     */
    void sync();

    /**
     * Synchronize pipeline and return all responses.
     *
     * @return A list of all the responses in the order you executed them.
     */
    List<Object> syncAndReturnAll();
}
