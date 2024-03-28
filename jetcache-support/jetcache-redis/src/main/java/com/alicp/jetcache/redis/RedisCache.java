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
import redis.clients.jedis.ClusterPipeline;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.commands.KeyBinaryCommands;
import redis.clients.jedis.commands.KeyPipelineBinaryCommands;
import redis.clients.jedis.commands.StringBinaryCommands;
import redis.clients.jedis.commands.StringPipelineBinaryCommands;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.providers.ClusterConnectionProvider;
import redis.clients.jedis.util.Pool;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    ClusterConnectionProvider provider = null;

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
        UnifiedJedis jedis = config.getJedis();
        if (jedis != null && jedis instanceof JedisCluster) {
            try {
                Field field = UnifiedJedis.class.getDeclaredField("provider");
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                provider = (ClusterConnectionProvider) field.get(jedis);
                field.setAccessible(accessible);
            } catch (Exception ex) {
                throw new IllegalStateException("can not get ConnectionProvider from JedisClient", ex);
            }
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
            if (r < x) {
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
        if (keys == null || keys.isEmpty()) {
            return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, Collections.emptyMap());
        }
        // define the result object early to gain statefulFunction feature.
        Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
        StringBinaryCommands readCommands = (StringBinaryCommands) readCommands();

        return this.<StringBinaryCommands, StringPipelineBinaryCommands, MultiGetResult<K, V>>doWithPipeline(readCommands, false, (pipeline, ex) -> {
            if (ex != null) {
                logError("GET_ALL", "keys(" + keys.size() + ")", ex);
                if (!resultMap.isEmpty()) {
                    return new MultiGetResult<K, V>(CacheResultCode.PART_SUCCESS, ex.toString(), resultMap);
                } else {
                    return new MultiGetResult<K, V>(ex);
                }
            }
            ArrayList<K> keyList = new ArrayList<K>(keys);
            byte[][] newKeys = keyList.stream().map(this::buildKey).toArray(byte[][]::new);
            List<byte[]> results;
            if (pipeline != null) {
                List<Response<byte[]>> responseList = new ArrayList<>();
                // Which is faster between pipeline.get or Jedis.mget()?
                for (byte[] newKey : newKeys) {
                    Response<byte[]> response = pipeline.get(newKey);
                    responseList.add(response);
                }

                sync(pipeline);

                results = responseList.stream().map(Response::get).collect(Collectors.toList());
            } else {
                results = readCommands.mget(newKeys);
            }

            return this.getAllResultAssemble(keyList, results, resultMap);
        });
    }

    private MultiGetResult<K, V> getAllResultAssemble(List<? extends K> keyList, List<byte[]> results, Map<K, CacheGetResult<V>> resultMap) {
        for (int i = 0; i < results.size(); i++) {
            Object value = results.get(i);
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

        return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, resultMap);
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
        if (map == null || map.isEmpty()) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        }
        StringBinaryCommands writeCommands = (StringBinaryCommands) writeCommands();
        return this.<StringBinaryCommands, StringPipelineBinaryCommands, CacheResult>doWithPipeline(writeCommands, true, (pipeline, ex) -> {
            if (ex != null) {
                logError("PUT_ALL", "map(" + map.size() + ")", ex);
                return new CacheResult(ex);
            }
            return pipelinePutAll(pipeline, map, expireAfterWrite, timeUnit);
        });
    }


    private CacheResult pipelinePutAll(StringPipelineBinaryCommands pipeline, Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        int failCount = 0;
        List<Response<String>> responses = new ArrayList<>();
        for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
            CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), timeUnit.toMillis(expireAfterWrite));
            Response<String> resp = pipeline.psetex(buildKey(en.getKey()), timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
            responses.add(resp);
        }

        sync(pipeline);

        for (Response<String> resp : responses) {
            if (!"OK".equals(resp.get())) {
                failCount++;
            }
        }
        return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                failCount == map.size() ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
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
        if (keys == null || keys.isEmpty()) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        }
        KeyBinaryCommands writeCommands = (KeyBinaryCommands) writeCommands();
        AtomicLong x = new AtomicLong();
        return this.<KeyBinaryCommands, KeyPipelineBinaryCommands, CacheResult>doWithPipeline(writeCommands, false, (pipeline, ex) -> {
            if (ex != null) {
                logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
                if (x.get() > 0) {
                    return new CacheResult(CacheResultCode.PART_SUCCESS, ex.toString());
                } else {
                    return new CacheResult(ex);
                }
            }
            byte[][] newKeys = keys.stream().map((k) -> buildKey(k)).toArray((len) -> new byte[keys.size()][]);

            if (pipeline != null) {
                for (byte[] newKey : newKeys) {
                    pipeline.del(newKey);
                    x.getAndIncrement();
                }

                sync(pipeline);
            } else {
                writeCommands.del(newKeys);
            }

            return CacheResult.SUCCESS_WITHOUT_MSG;
        });
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

    /**
     * Useful function for redis interaction via Pipeline mode. Resources will be auto close. <br/>
     * For batch command, such as {@link StringBinaryCommands#mget(byte[]...)}, {@link KeyBinaryCommands#del(byte[]...)}, the performance of pipeline will be worse,
     * so choose them instead of pipeline as much as possible in non-cluster clients, See:
     * - https://medium.com/@jychen7/redis-get-pipeline-vs-mget-6e41aeaecef
     * - https://stackoverflow.com/questions/73992769/redis-del-many-keys-vs-pipeline-are-both-non-blocking
     *
     * @param client        redisClient
     * @param pipelineFirst set as false when only want to use the pipeline on cluster clients.
     * @param biFunction    callback
     * @param <C>           client type
     * @param <R>           result type
     * @return result
     */
    @SuppressWarnings("unchecked")
    private <C, P, R> R doWithPipeline(C client, boolean pipelineFirst, BiFunction<P, Exception, R> biFunction) {
        C commands = null;
        Closeable closeable = null;
        try {
            commands = client;
            P pipeline;
            // The connection from JedisPooled or JedisCluster needs to be returned to the pool.
            if (commands instanceof JedisCluster) {
                ClusterPipeline clusterPipeline = new ClusterPipeline(provider);
                closeable = clusterPipeline;
                pipeline = (P) clusterPipeline;
            } else if (commands instanceof JedisPooled && pipelineFirst) {
                Connection connection = ((JedisPooled) commands).getPool().getResource();
                closeable = connection;
                pipeline = (P) new Pipeline(connection);
            } else if (commands instanceof Jedis && pipelineFirst) {
                pipeline = (P) new Pipeline((Jedis) commands);
            } else {
                // use the client rather than pipeline
                pipeline = null;
            }

            return biFunction.apply(pipeline, null);
        } catch (Exception ex) {
            return biFunction.apply(null, ex);
        } finally {
            closeJedis(commands);
            close(closeable);
        }
    }

    private <T> void sync(T pipeline) {
        if (pipeline instanceof Pipeline) {
            ((Pipeline) pipeline).sync();
        } else if (pipeline instanceof ClusterPipeline) {
            ((ClusterPipeline) pipeline).sync();
        } else {
            throw new UnsupportedOperationException("unrecognized pipeline type");
        }
    }

}
