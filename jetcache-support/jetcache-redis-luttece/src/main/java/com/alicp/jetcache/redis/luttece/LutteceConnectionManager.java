package com.alicp.jetcache.redis.luttece;

import com.alicp.jetcache.CacheConfigException;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulConnection;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import com.lambdaworks.redis.cluster.api.async.RedisClusterAsyncCommands;
import com.lambdaworks.redis.cluster.api.rx.RedisClusterReactiveCommands;
import com.lambdaworks.redis.cluster.api.sync.RedisClusterCommands;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LutteceConnectionManager {

    private static class LutteceObjects {
        private StatefulConnection connection;
        private Object commands;
        private Object asyncCommands;
        private Object reactiveCommands;
    }

    private static final LutteceConnectionManager defaultManager = new LutteceConnectionManager();

    private Map map = Collections.synchronizedMap(new WeakHashMap());

    private LutteceConnectionManager() {
    }

    public static LutteceConnectionManager defaultManager() {
        return defaultManager;
    }

    private LutteceObjects getLutteceObjectsFromMap(AbstractRedisClient redisClient) {
        return (LutteceObjects) map.computeIfAbsent(redisClient, key -> new LutteceObjects());
    }

    public StatefulConnection connection(AbstractRedisClient redisClient) {
        LutteceObjects lo = getLutteceObjectsFromMap(redisClient);
        if (lo.connection == null) {
            if (redisClient instanceof RedisClient) {
                lo.connection = ((RedisClient) redisClient).connect(new JetCacheCodec());
            } else if (redisClient instanceof RedisClusterClient) {
                lo.connection = ((RedisClusterClient) redisClient).connect(new JetCacheCodec());
            } else {
                throw new CacheConfigException("type " + redisClient.getClass() + " is not supported");
            }
        }
        return lo.connection;
    }

    public Object commands(AbstractRedisClient redisClient) {
        connection(redisClient);
        LutteceObjects lo = getLutteceObjectsFromMap(redisClient);
        if (lo.commands == null) {
            if (lo.connection instanceof StatefulRedisConnection) {
                lo.commands = ((StatefulRedisConnection) lo.connection).sync();
            } else if (lo.connection instanceof StatefulRedisClusterConnection) {
                lo.commands = ((StatefulRedisClusterConnection) lo.connection).sync();
            } else {
                throw new CacheConfigException("type " + lo.connection.getClass() + " is not supported");
            }
        }
        return lo.commands;
    }


    public Object asyncCommands(AbstractRedisClient redisClient) {
        connection(redisClient);
        LutteceObjects lo = getLutteceObjectsFromMap(redisClient);
        if (lo.asyncCommands == null) {
            if (lo.connection instanceof StatefulRedisConnection) {
                lo.asyncCommands = ((StatefulRedisConnection) lo.connection).async();
            } else if (lo.connection instanceof StatefulRedisClusterConnection) {
                lo.asyncCommands = ((StatefulRedisClusterConnection) lo.connection).async();
            } else {
                throw new CacheConfigException("type " + lo.connection.getClass() + " is not supported");
            }
        }
        return lo.asyncCommands;
    }

    public Object reactiveCommands(AbstractRedisClient redisClient) {
        connection(redisClient);
        LutteceObjects lo = getLutteceObjectsFromMap(redisClient);
        if (lo.reactiveCommands == null) {
            if (lo.connection instanceof StatefulRedisConnection) {
                lo.reactiveCommands = ((StatefulRedisConnection) lo.connection).reactive();
            } else if (lo.connection instanceof StatefulRedisClusterConnection) {
                lo.reactiveCommands = ((StatefulRedisClusterConnection) lo.connection).reactive();
            } else {
                throw new CacheConfigException("type " + lo.connection.getClass() + " is not supported");
            }
        }
        return lo.reactiveCommands;
    }

    public void removeAndClose(AbstractRedisClient redisClient) {
        LutteceObjects lo = (LutteceObjects) map.remove(redisClient);
        if (lo == null) {
            return;
        }
        if (lo.commands != null && lo.commands instanceof RedisClusterCommands) {
            ((RedisClusterCommands) lo.commands).close();
        }
        if (lo.asyncCommands != null && lo.asyncCommands instanceof RedisClusterAsyncCommands) {
            ((RedisClusterAsyncCommands) lo.asyncCommands).close();
        }
        if (lo.reactiveCommands != null && lo.reactiveCommands instanceof RedisClusterReactiveCommands) {
            ((RedisClusterReactiveCommands) lo.reactiveCommands).close();
        }
        if (lo.connection != null) {
            lo.connection.close();
        }
    }
}
