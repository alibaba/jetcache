package com.alicp.jetcache.redis.lettuce4;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.JetCacheExecutor;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.SetArgs;
import com.lambdaworks.redis.api.async.RedisKeyAsyncCommands;
import com.lambdaworks.redis.api.async.RedisStringAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.lambdaworks.redis.cluster.api.async.RedisClusterAsyncCommands;
import com.lambdaworks.redis.cluster.api.rx.RedisClusterReactiveCommands;
import com.lambdaworks.redis.cluster.api.sync.RedisClusterCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2017/4/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisLettuceCache<K, V> extends AbstractExternalCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(RedisLettuceCache.class);

    private RedisLettuceCacheConfig<K, V> config;

    private Function<Object, byte[]> valueEncoder;
    private Function<byte[], Object> valueDecoder;

    private final AbstractRedisClient client;
    private LettuceConnectionManager lettuceConnectionManager;
    private RedisStringCommands<byte[], byte[]> stringCommands;
    private RedisStringAsyncCommands<byte[], byte[]> stringAsyncCommands;
    private RedisKeyAsyncCommands<byte[], byte[]> keyAsyncCommands;

    public RedisLettuceCache(RedisLettuceCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();
        if (config.getRedisClient() == null) {
            throw new CacheConfigException("RedisClient is required");
        }
        if (config.isExpireAfterAccess()) {
            throw new CacheConfigException("expireAfterAccess is not supported");
        }

        client = config.getRedisClient();
        lettuceConnectionManager = LettuceConnectionManager.defaultManager();

        stringCommands = (RedisStringCommands<byte[], byte[]>) lettuceConnectionManager.commands(client);
        stringAsyncCommands = (RedisStringAsyncCommands<byte[], byte[]>) lettuceConnectionManager.asyncCommands(client);
        keyAsyncCommands = (RedisKeyAsyncCommands<byte[], byte[]>) stringAsyncCommands;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        if (AbstractRedisClient.class.isAssignableFrom(clazz)) {
            return (T) client;
        } else if (RedisClusterCommands.class.isAssignableFrom(clazz)) {
            // RedisCommands extends RedisClusterCommands
            return (T) stringCommands;
        } else if (RedisClusterAsyncCommands.class.isAssignableFrom(clazz)) {
            // RedisAsyncCommands extends RedisClusterAsyncCommands
            return (T) stringAsyncCommands;
        } else if (RedisClusterReactiveCommands.class.isAssignableFrom(clazz)) {
            // RedisReactiveCommands extends RedisClusterReactiveCommands
            return (T) lettuceConnectionManager.reactiveCommands(client);
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            RedisFuture<String> future = stringAsyncCommands.psetex(newKey, timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
            return new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("PUT", key, ex));
                    return new ResultData(ex);
                } else {
                    if ("OK".equals(rt)) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else {
                        return new ResultData(CacheResultCode.FAIL, rt, null);
                    }
                }
            }));
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        if (map == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try {
            CompletionStage<Integer> future = CompletableFuture.completedFuture(0);
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), timeUnit.toMillis(expireAfterWrite));
                RedisFuture<String> resp = stringAsyncCommands.psetex(buildKey(en.getKey()), timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
                future = future.thenCombine(resp, (failCount, respStr) -> "OK".equals(respStr) ? failCount : failCount + 1);
            }
            return new CacheResult(future.handle((failCount, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("PUT_ALL", "map(" + map.size() + ")", ex));
                    return new ResultData(ex);
                } else {
                    if (failCount == 0) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else if (failCount == map.size()) {
                        return new ResultData(CacheResultCode.FAIL, null, null);
                    } else {
                        return new ResultData(CacheResultCode.PART_SUCCESS, null, null);
                    }
                }
            }));
        } catch (Exception ex) {
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        if (key == null) {
            return new CacheGetResult<V>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        try {
            byte[] newKey = buildKey(key);
            RedisFuture<byte[]> future = stringAsyncCommands.get(newKey);
            return new CacheGetResult(future.handle((valueBytes, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("GET", key, ex));
                    return new ResultData(ex);
                } else {
                    if (valueBytes != null) {
                        CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(valueBytes);
                        if (System.currentTimeMillis() >= holder.getExpireTime()) {
                            return new ResultData(CacheResultCode.EXPIRED, null, null);
                        } else {
                            return new ResultData(CacheResultCode.SUCCESS, null, holder);
                        }
                    } else {
                        return new ResultData(CacheResultCode.NOT_EXISTS, null, null);
                    }
                }
            }));
        } catch (Exception ex) {
            logError("GET", key, ex);
            return new CacheGetResult(ex);
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        try {
            ArrayList<K> keyList = new ArrayList<K>(keys);
            byte[][] newKeys = keyList.stream().map((k) -> buildKey(k)).toArray(byte[][]::new);

            Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
            if (newKeys.length == 0) {
                return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, resultMap);
            }
            RedisFuture<List<byte[]>> mgetResults = stringAsyncCommands.mget(newKeys);
            return new MultiGetResult<K, V>(mgetResults.handle((list, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("GET_ALL", "keys(" + keys.size() + ")", ex));
                    return new ResultData(ex);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        Object value = list.get(i);
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
                    return new ResultData(CacheResultCode.SUCCESS, null, resultMap);
                }
            }));
        } catch (Exception ex) {
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<K, V>(ex);
        }
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        if (key == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try {
            RedisFuture<Long> future = keyAsyncCommands.del(buildKey(key));
            return new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("REMOVE", key, ex));
                    return new ResultData(ex);
                } else {
                    if (rt == null) {
                        return new ResultData(CacheResultCode.FAIL, null, null);
                    } else if (rt == 1) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else if (rt == 0) {
                        return new ResultData(CacheResultCode.NOT_EXISTS, null, null);
                    } else {
                        return new ResultData(CacheResultCode.FAIL, null, null);
                    }
                }
            }));
        } catch (Exception ex) {
            logError("REMOVE", key, ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return CacheResult.FAIL_ILLEGAL_ARGUMENT;
        }
        try {
            byte[][] newKeys = keys.stream().map((k) -> buildKey(k)).toArray((len) -> new byte[keys.size()][]);
            RedisFuture<Long> future = keyAsyncCommands.del(newKeys);
            return new CacheResult(future.handle((v, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex));
                    return new ResultData(ex);
                } else {
                    return new ResultData(CacheResultCode.SUCCESS, null, null);
                }
            }));
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
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            RedisFuture<String> future = stringAsyncCommands.set(newKey, valueEncoder.apply(holder), SetArgs.Builder.nx().px(timeUnit.toMillis(expireAfterWrite)));
            return new CacheResult(future.handle((rt, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("PUT_IF_ABSENT", key, ex));
                    return new ResultData(ex);
                } else {
                    if ("OK".equals(rt)) {
                        return new ResultData(CacheResultCode.SUCCESS, null, null);
                    } else if (rt == null) {
                        return new ResultData(CacheResultCode.EXISTS, null, null);
                    } else {
                        return new ResultData(CacheResultCode.FAIL, rt , null);
                    }
                }
            }));
        } catch (Exception ex) {
            logError("PUT_IF_ABSENT", key, ex);
            return new CacheResult(ex);
        }
    }

}
