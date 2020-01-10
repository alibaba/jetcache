package com.alicp.jetcache.redis.jedis;

import com.alicp.jetcache.CacheException;
import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisClusterConnectionHandler;
import redis.clients.jedis.JedisClusterInfoCache;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSlotBasedConnectionHandler;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.util.JedisClusterCRC16;
import redis.clients.jedis.util.SafeEncoder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created on 2019/12/12.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public class JedisClusterPipeline extends Pipeline implements AbstractJedisPipeline {

    private static final Field FIELD_CONNECTION_HANDLER;
    private static final Field FIELD_CACHE;

    static {
        FIELD_CONNECTION_HANDLER = getField(BinaryJedisCluster.class, "connectionHandler");
        FIELD_CACHE = getField(JedisClusterConnectionHandler.class, "cache");
    }

    private JedisSlotBasedConnectionHandler connectionHandler;
    private JedisClusterInfoCache clusterInfoCache;

    private Queue<Client> clients = new LinkedList();
    private Map<JedisPool, Jedis> poolResourceMap = new HashMap();

    public JedisClusterPipeline(final JedisCluster jedisCluster) {
        init(jedisCluster);
    }

    private void init(final JedisCluster jedisCluster) {
        connectionHandler = getValue(jedisCluster, FIELD_CONNECTION_HANDLER);
        clusterInfoCache = getValue(connectionHandler, FIELD_CACHE);
    }

    @Override
    public void sync() {
        Iterator<Client> clientIterator = clients.iterator();
        while (clientIterator.hasNext()) {
            Client client = clientIterator.next();
            generateResponse(client.getOne());
            clientIterator.remove();
        }
    }

    @Override
    public List<Object> syncAndReturnAll() {
        List<Object> formatted = new ArrayList<Object>();
        Iterator<Client> clientIterator = clients.iterator();
        while (clientIterator.hasNext()) {
            Client client = clientIterator.next();
            formatted.add(generateResponse(client.getOne()));
            clientIterator.remove();
        }
        return formatted;
    }

    @Override
    public void close() {
        super.close();
        closeJedises();
        clean();
        clients.clear();
        poolResourceMap.clear();
    }

    private void closeJedises() {
        for (Jedis jedis : poolResourceMap.values()) {
            jedis.close();
        }
    }

    @Override
    protected Client getClient(final String key) {
        return getClient(SafeEncoder.encode(key));
    }

    @Override
    protected Client getClient(final byte[] key) {
        Jedis jedis = getJedis(JedisClusterCRC16.getSlot(key));
        Client client = jedis.getClient();
        clients.add(client);
        return client;
    }

    private Jedis getJedis(final int slot) {
        JedisPool pool = clusterInfoCache.getSlotPool(slot);
        return poolResourceMap.computeIfAbsent(pool, key -> key.getResource());
    }

    private static Field getField(final Class<?> cls, final String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            throw new CacheException(
                    String.format("Can not find or access field '%s' from '%s' .", fieldName, cls.getName()), e);
        }
    }

    private static <T> T getValue(final Object obj, final Field field) {
        try {
            return (T) field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
