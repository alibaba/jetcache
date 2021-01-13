package com.alicp.jetcache.redis.lettuce;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.support.JetCacheExecutor;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.reactive.RedisClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

import java.time.Duration;
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
        lettuceConnectionManager.init(client, config.getConnection());
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

    private void setTimeout(CacheResult cr) {
        Duration d = Duration.ofMillis(config.getAsyncResultTimeoutInMillis());
        cr.setTimeout(d);
    }

    @Override
    protected CacheResult do_PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            RedisFuture<String> future = stringAsyncCommands.psetex(newKey, timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
            CacheResult result = new CacheResult(future.handle((rt, ex) -> {
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
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit) {
        try {
            CompletionStage<Integer> future = CompletableFuture.completedFuture(0);
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), timeUnit.toMillis(expireAfterWrite));
                RedisFuture<String> resp = stringAsyncCommands.psetex(buildKey(en.getKey()), timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
                future = future.thenCombine(resp, (failCount, respStr) -> "OK".equals(respStr) ? failCount : failCount + 1);
            }
            CacheResult result = new CacheResult(future.handle((failCount, ex) -> {
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
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheGetResult<V> do_GET(K key) {
        try {
            byte[] newKey = buildKey(key);
            RedisFuture<byte[]> future = stringAsyncCommands.get(newKey);
            CacheGetResult result = new CacheGetResult(future.handle((valueBytes, ex) -> {
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
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("GET", key, ex);
            return new CacheGetResult(ex);
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(Set<? extends K> keys) {
        try {
            ArrayList<K> keyList = new ArrayList<K>(keys);
            byte[][] newKeys = keyList.stream().map((k) -> buildKey(k)).toArray(byte[][]::new);

            Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
            if (newKeys.length == 0) {
                return new MultiGetResult<K, V>(CacheResultCode.SUCCESS, null, resultMap);
            }
            RedisFuture<List<KeyValue<byte[],byte[]>>> mgetResults = stringAsyncCommands.mget(newKeys);
            MultiGetResult result = new MultiGetResult<K, V>(mgetResults.handle((list, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("GET_ALL", "keys(" + keys.size() + ")", ex));
                    return new ResultData(ex);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        KeyValue kv = list.get(i);
                        K key = keyList.get(i);
                        if (kv != null && kv.hasValue()) {
                            CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply((byte[]) kv.getValue());
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
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<K, V>(ex);
        }
    }

    @Override
    protected CacheResult do_REMOVE(K key) {
        try {
            RedisFuture<Long> future = keyAsyncCommands.del(buildKey(key));
            CacheResult result = new CacheResult(future.handle((rt, ex) -> {
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
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("REMOVE", key, ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(Set<? extends K> keys) {
        try {
            byte[][] newKeys = keys.stream().map((k) -> buildKey(k)).toArray((len) -> new byte[keys.size()][]);
            RedisFuture<Long> future = keyAsyncCommands.del(newKeys);
            CacheResult result = new CacheResult(future.handle((v, ex) -> {
                if (ex != null) {
                    JetCacheExecutor.defaultExecutor().execute(() -> logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex));
                    return new ResultData(ex);
                } else {
                    return new ResultData(CacheResultCode.SUCCESS, null, null);
                }
            }));
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit) {
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            RedisFuture<String> future = stringAsyncCommands.set(newKey, valueEncoder.apply(holder), SetArgs.Builder.nx().px(timeUnit.toMillis(expireAfterWrite)));
            CacheResult result = new CacheResult(future.handle((rt, ex) -> {
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
            setTimeout(result);
            return result;
        } catch (Exception ex) {
            logError("PUT_IF_ABSENT", key, ex);
            return new CacheResult(ex);
        }
    }
}
