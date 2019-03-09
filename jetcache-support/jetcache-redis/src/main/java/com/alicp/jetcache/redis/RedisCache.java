package com.alicp.jetcache.redis;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisCache<K, V> extends AbstractExternalCache<K, V> {

    private RedisCacheConfig<K, V> config;

    Function<Object, byte[]> valueEncoder;
    Function<byte[], Object> valueDecoder;

    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public RedisCache(RedisCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();

        if (config.getJedisPool() == null) {
            throw new CacheConfigException("no pool");
        }
        if (config.isReadFromSlave()) {
            if (config.getJedisSlavePools() == null || config.getJedisSlavePools().length == 0) {
                throw new CacheConfigException("slaves not config");
            }
            if (config.getSlaveReadWeights() == null) {
                int len = config.getJedisSlavePools().length;
                int[] weights = new int[len];
                Arrays.fill(weights, 100);
                config.setSlaveReadWeights(weights);
            }
        }
        if (config.isExpireAfterAccess()) {
            throw new CacheConfigException("expireAfterAccess is not supported");
        }
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (Pool.class.isAssignableFrom(clazz)) {
            return (T) config.getJedisPool();
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    Pool getReadPool() {
        if (!config.isReadFromSlave()) {
            return config.getJedisPool();
        }
        int[] weights = config.getSlaveReadWeights();
        int index = randomIndex(weights);
        return config.getJedisSlavePools()[index];
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

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        if (key == null) {
            return new CacheGetResult<V>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        try (AutoCloseable resource = (AutoCloseable) getReadPool().getResource()) {
            BinaryJedisCommands commands = (BinaryJedisCommands) resource;
            return do_GET_impl(key, commands);
        } catch (Exception ex) {
            logError("GET", key, ex);
            return new CacheGetResult(ex);
        }
    }

    private CacheGetResult<V> do_GET_impl(K key, BinaryJedisCommands commands) {
        byte[] newKey = buildKey(key);
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
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        try (AutoCloseable resource = (AutoCloseable) getReadPool().getResource()) {
            Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
            if (resource instanceof MultiKeyBinaryCommands) {
                MultiKeyBinaryCommands jedis = (MultiKeyBinaryCommands) resource;
                ArrayList<K> keyList = new ArrayList<K>(keys);
                byte[][] newKeys = keyList.stream().map((k) -> buildKey(k)).toArray(byte[][]::new);

                if (newKeys.length > 0) {
                    List mgetResults = jedis.mget(newKeys);
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
                return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, resultMap);
            } else { // for ShardedJedis
                BinaryJedisCommands commands = (BinaryJedisCommands) resource;
                int failCount = 0;
                for (K k : keys) {
                    CacheGetResult<V> r = do_GET_impl(k, commands);
                    resultMap.put(k, r);
                    if (r.getResultCode() == CacheResultCode.FAIL || r.getResultCode() == CacheResultCode.PART_SUCCESS) {
                        failCount++;
                    }
                }
                CacheResultCode code = failCount == 0 ? CacheResultCode.SUCCESS :
                        failCount == keys.size() ? CacheResultCode.FAIL : CacheResultCode.PART_SUCCESS;
                return new MultiGetResult<>(code, null, resultMap);
            }
        } catch (Exception ex) {
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<K, V>(ex);
        }
    }


    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (AutoCloseable resource = (AutoCloseable) config.getJedisPool().getResource()) {
            BinaryJedisCommands commands = (BinaryJedisCommands) resource;
            return do_PUT_impl(key, value, expireAfterWrite, timeUnit, commands);
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(ex);
        }
    }

    private CacheResult do_PUT_impl(K key, V value, long expireAfterWrite, TimeUnit timeUnit, BinaryJedisCommands commands) {
        CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
        byte[] newKey = buildKey(key);
        String rt = commands.psetex(newKey, timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
        if ("OK".equals(rt)) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } else {
            return new CacheResult(CacheResultCode.FAIL, rt);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (AutoCloseable resource = (AutoCloseable) config.getJedisPool().getResource()) {
            int failCount = 0;
            if (resource instanceof Jedis) {
                Jedis jedis = (Jedis) resource;

                List<Response<String>> responses = new ArrayList<>();
                Pipeline p = jedis.pipelined();
                for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                    CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), timeUnit.toMillis(expireAfterWrite));
                    Response<String> resp = p.psetex(buildKey(en.getKey()), timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
                    responses.add(resp);
                }
                p.sync();
                for (Response<String> resp : responses) {
                    if (!"OK".equals(resp.get())) {
                        failCount++;
                    }
                }
            } else { // for ShardedJedis
                BinaryJedisCommands commands = (BinaryJedisCommands) resource;
                for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                    CacheResult r = do_PUT_impl(en.getKey(), en.getValue(),
                            expireAfterWrite, TimeUnit.MILLISECONDS, commands);
                    if (r.getResultCode() == CacheResultCode.FAIL || r.getResultCode() == CacheResultCode.PART_SUCCESS) {
                        failCount++;
                    }
                }
            }
            return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                    failCount == map.size() ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (AutoCloseable resource = (AutoCloseable) config.getJedisPool().getResource()) {
            BinaryJedisCommands jedis = (BinaryJedisCommands) resource;
            return do_REMOVE_impl(key, jedis);
        } catch (Exception ex) {
            logError("REMOVE", key, ex);
            return new CacheResult(ex);
        }
    }

    private CacheResult do_REMOVE_impl(K key, BinaryJedisCommands commands) {
        Long rt = commands.del(buildKey(key));
        if (rt == null) {
            return CacheResult.FAIL_WITHOUT_MSG;
        } else if (rt == 1) {
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } else if (rt == 0) {
            return new CacheResult(CacheResultCode.NOT_EXISTS, null);
        } else {
            return CacheResult.FAIL_WITHOUT_MSG;
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (AutoCloseable resource = (AutoCloseable) config.getJedisPool().getResource()) {
            if (resource instanceof MultiKeyBinaryCommands) {
                MultiKeyBinaryCommands commands = (MultiKeyBinaryCommands) resource;
                byte[][] newKeys = keys.stream().map((k) -> buildKey(k)).toArray((len) -> new byte[keys.size()][]);
                commands.del(newKeys);
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else { // for ShardedJedis
                int failCount = 0;
                BinaryJedisCommands jedisCommands = (BinaryJedisCommands) resource;
                for(K k: keys) {
                    CacheResult r = do_REMOVE_impl(k, jedisCommands);
                    if (r.getResultCode() == CacheResultCode.FAIL || r.getResultCode() == CacheResultCode.PART_SUCCESS) {
                        failCount++;
                    }
                }
                return failCount == 0 ? CacheResult.SUCCESS_WITHOUT_MSG :
                        failCount == keys.size() ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
            }
        } catch (Exception ex) {
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try (AutoCloseable resource = (AutoCloseable) config.getJedisPool().getResource()) {
            BinaryJedisCommands commands = (BinaryJedisCommands) resource;
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            String rt = commands.set(newKey, valueEncoder.apply(holder), "NX".getBytes(), "PX".getBytes(), timeUnit.toMillis(expireAfterWrite));
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
        }
    }

    @Override
    protected boolean needLogStackTrace(Throwable e) {
        if (e instanceof JedisConnectionException) {
            return false;
        }
        return true;
    }
}
