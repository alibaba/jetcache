package com.alicp.jetcache.redis;

import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.MultiGetResult;
import com.alicp.jetcache.external.AbstractExternalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.commands.KeyBinaryCommands;
import redis.clients.jedis.commands.StringBinaryCommands;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/7.
 *
 * @author huangli
 */
public class RedisCache<K, V> extends AbstractExternalCache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(RedisCache.class);

    protected RedisCacheConfig<K, V> config;

    Function<Object, byte[]> valueEncoder;
    Function<byte[], Object> valueDecoder;

    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public RedisCache(RedisCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();

        if (config.getJedis() == null && config.getJedisPool() == null) {
            throw new CacheConfigException("no jedis");
        }
        if (config.getJedis() != null && config.getJedisPool() != null) {
            throw new CacheConfigException("'jedis' and 'jedisPool' can't set simultaneously");
        }
        if (config.getJedis() != null && config.getJedisSlavePools() != null) {
            throw new CacheConfigException("'jedisSlavePools' should work with 'jedisPool' in RedisCacheConfig");
        }
        if (config.getJedisPool() != null && config.getSlaves() != null) {
            throw new CacheConfigException("'slaves' should work with 'jedis' in RedisCacheConfig");
        }
        if (config.isReadFromSlave()) {
            if (slaveCount() == 0) {
                throw new CacheConfigException("slaves not config");
            }
            if (config.getSlaveReadWeights() == null) {
                initDefaultWeights();
            } else if (config.getSlaveReadWeights().length != slaveCount()) {
                logger.error("length of slaveReadWeights and jedisSlavePools not equals, using default weights");
                initDefaultWeights();
            }
        }
        if (config.isExpireAfterAccess()) {
            throw new CacheConfigException("expireAfterAccess is not supported");
        }
    }

    private int slaveCount() {
        if (config.getSlaves() != null) {
            return config.getSlaves().length;
        }
        if (config.getJedisSlavePools() != null) {
            return config.getJedisSlavePools().length;
        }
        return 0;
    }

    private void initDefaultWeights() {
        int len = slaveCount();
        int[] weights = new int[len];
        Arrays.fill(weights, 100);
        config.setSlaveReadWeights(weights);
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (UnifiedJedis.class.isAssignableFrom(clazz)) {
            return (T) config.getJedis();
        }
        if (Pool.class.isAssignableFrom(clazz)) {
            return (T) config.getJedisPool();
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    Object writeCommands() {
        return config.getJedis() != null ? config.getJedis() : config.getJedisPool().getResource();
    }

    Object readCommands() {
        if (!config.isReadFromSlave()) {
            return writeCommands();
        }
        int[] weights = config.getSlaveReadWeights();
        int index = randomIndex(weights);
        if (config.getSlaves() != null) {
            return config.getSlaves()[index];
        } else {
            return config.getJedisSlavePools()[index].getResource();
        }
    }

    static int randomIndex(int[] weights) {
        int sumOfWeights = 0;
        for (int w : weights) {
            sumOfWeights += w;
        }
        int r = random.nextInt(sumOfWeights);
        int x = 0;
        for (int i = 0; i < weights.length; i++) {
            x += weights[i];
            if(r < x){
                return i;
            }
        }
        throw new CacheException("assert false");
    }

    static void closeJedis(Object maybeJedis) {
        if (maybeJedis instanceof Jedis) {
            close((Jedis) maybeJedis);
        }
    }

    private static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            logger.warn("close jedis resource error: {}", e.toString());
        }
    }

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        StringBinaryCommands commands = null;
        try {
            byte[] newKey = buildKey(key);
            commands = (StringBinaryCommands) readCommands();
            byte[] bytes = commands.get(newKey);
            if (bytes != null) {
                CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(bytes);
                if (System.currentTimeMillis() >= holder.getExpireTime()) {
                    return CacheGetResult.EXPIRED_WITHOUT_MSG;
                }
                return new CacheGetResult(CacheResultCode.SUCCESS, null, holder);
            } else {
                return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
            }
        } catch (Exception ex) {
            logError("GET", key, ex);
            return new CacheGetResult(ex);
        } finally {
            closeJedis(commands);
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        StringBinaryCommands commands = null;
        Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
        try {
            ArrayList<K> keyList = new ArrayList<K>(keys);
            byte[][] newKeys = keyList.stream().map((k) -> buildKey(k)).toArray(byte[][]::new);

            if (newKeys.length > 0) {
                commands = (StringBinaryCommands) readCommands();
                if (commands instanceof JedisCluster) {
                    jedisClusterGetAll((JedisCluster) commands, resultMap, keyList, newKeys);
                } else {
                    List mgetResults = commands.mget(newKeys);
                    for (int i = 0; i < mgetResults.size(); i++) {
                        Object value = mgetResults.get(i);
                        K key = keyList.get(i);
                        if (value != null) {
                            CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply((byte[]) value);
                            if (System.currentTimeMillis() >= holder.getExpireTime()) {
                                resultMap.put(key, CacheGetResult.EXPIRED_WITHOUT_MSG);
                            } else {
                                CacheGetResult<V> r = new CacheGetResult<V>(CacheResultCode.SUCCESS, null, holder);
                                resultMap.put(key, r);
                            }
                        } else {
                            resultMap.put(key, CacheGetResult.NOT_EXISTS_WITHOUT_MSG);
                        }
                    }
                }
            }
            return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, resultMap);
        } catch (Exception ex) {
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            if (resultMap.size() > 0) {
                return new MultiGetResult<K, V>(CacheResultCode.PART_SUCCESS, ex.toString(), resultMap);
            } else {
                return new MultiGetResult<K, V>(ex);
            }
        } finally {
            closeJedis(commands);
        }
    }

    private void jedisClusterGetAll(JedisCluster commands, Map<K, CacheGetResult<V>> resultMap, ArrayList<K> keyList, byte[][] newKeys) {
        for (int i = 0; i < newKeys.length; i++) {
            byte[] bytes = commands.get(newKeys[i]);
            if (bytes != null) {
                CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(bytes);
                if (System.currentTimeMillis() >= holder.getExpireTime()) {
                    resultMap.put(keyList.get(i), CacheGetResult.EXPIRED_WITHOUT_MSG);
                } else {
                    resultMap.put(keyList.get(i), new CacheGetResult<V>(CacheResultCode.SUCCESS, null, holder));
                }
            } else {
                resultMap.put(keyList.get(i), CacheGetResult.NOT_EXISTS_WITHOUT_MSG);
            }
        }
    }


    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        StringBinaryCommands commands = null;
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            commands = (StringBinaryCommands) writeCommands();
            String rt = commands.psetex(newKey, timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
            if ("OK".equals(rt)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, rt);
            }
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(ex);
        } finally {
            closeJedis(commands);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        StringBinaryCommands commands = null;
        Connection connection = null;
        try {
            commands = (StringBinaryCommands) writeCommands();
            int failCount = 0;
            if(commands instanceof Jedis || commands instanceof JedisPooled) {
                List<Response<String>> responses = new ArrayList<>();
                Pipeline pipeline = null;
                if(commands instanceof JedisPooled) {
                    connection = ((JedisPooled) commands).getPool().getResource();
                    pipeline = new Pipeline(connection);
                } else {
                    pipeline = new Pipeline((Jedis) commands);
                }
                for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                    CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), timeUnit.toMillis(expireAfterWrite));
                    Response<String> resp = pipeline.psetex(buildKey(en.getKey()), timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
                    responses.add(resp);
                }
                pipeline.sync();
                for (Response<String> resp : responses) {
                    if (!"OK".equals(resp.get())) {
                        failCount++;
                    }
                }
            } else {
                for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                    CacheResult r = do_PUT(en.getKey(), en.getValue(), expireAfterWrite, timeUnit);
                    if (!r.isSuccess()) {
                        failCount++;
                    }
                }
            }
            return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                    failCount == map.size() ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        } finally {
            closeJedis(commands);
            close(connection);
        }
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        return REMOVE_impl(key, buildKey(key));
    }

    private CacheResult REMOVE_impl(Object key, byte[] newKey) {
        KeyBinaryCommands commands = null;
        try {
            commands = (KeyBinaryCommands) writeCommands();
            Long rt = commands.del(newKey);
            if (rt == null) {
                return CacheResult.FAIL_WITHOUT_MSG;
            } else if (rt == 1) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else if (rt == 0) {
                return new CacheResult(CacheResultCode.NOT_EXISTS, null);
            } else {
                return CacheResult.FAIL_WITHOUT_MSG;
            }
        } catch (Exception ex) {
            logError("REMOVE", key, ex);
            return new CacheResult(ex);
        } finally {
            closeJedis(commands);
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        KeyBinaryCommands commands = null;
        int x = 0;
        try {
            byte[][] newKeys = keys.stream().map((k) -> buildKey(k)).toArray((len) -> new byte[keys.size()][]);
            commands = (KeyBinaryCommands) writeCommands();
            if (commands instanceof JedisCluster) {
                for (byte[] newKey : newKeys) {
                    commands.del(newKey);
                    x++;
                }
            } else {
                commands.del(newKeys);
            }
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
            if (commands instanceof JedisCluster && x > 0) {
                return new CacheResult(CacheResultCode.PART_SUCCESS, ex.toString());
            } else {
                return new CacheResult(ex);
            }
        } finally {
            closeJedis(commands);
        }
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        StringBinaryCommands commands = null;
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            SetParams params = new SetParams();
            params.nx()
                    .px(timeUnit.toMillis(expireAfterWrite));
            commands = (StringBinaryCommands) writeCommands();
            String rt = commands.set(newKey, valueEncoder.apply(holder), params);
            if ("OK".equals(rt)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else if (rt == null) {
                return CacheResult.EXISTS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, rt);
            }
        } catch (Exception ex) {
            logError("PUT_IF_ABSENT", key, ex);
            return new CacheResult(ex);
        } finally {
            closeJedis(commands);
        }
    }

}
