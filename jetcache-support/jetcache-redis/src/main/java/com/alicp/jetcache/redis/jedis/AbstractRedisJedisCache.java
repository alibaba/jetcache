package com.alicp.jetcache.redis.jedis;

import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.MultiGetResult;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.alicp.jetcache.external.ExternalCacheConfig;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2019/12/11.
 *
 * @author <a href="mailto:redzippo@foxmail.com">gezhen</a>
 * @author <a href="mailto:eason.fengys@gmail.com">eason.feng</a>
 */
public abstract class AbstractRedisJedisCache<K, V> extends AbstractExternalCache<K, V> {

    private ExternalCacheConfig<K, V> config;

    private Function<Object, byte[]> valueEncoder;
    private Function<byte[], Object> valueDecoder;

    public AbstractRedisJedisCache(final ExternalCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();
    }

    protected abstract byte[] jedisGet(byte[] key);

    protected abstract List<byte[]> jedisMget(byte[]... keys);

    protected abstract String jedisPsetex(byte[] key, long milliseconds, byte[] value);

    protected abstract Long jedisDel(byte[]... keys);

    protected abstract String jedisSet(byte[] key, byte[] value, SetParams params);

    protected abstract AbstractJedisPipeline getJedisPipeline();

    protected abstract boolean isEnablePipeline();

    @Override
    protected CacheGetResult<V> do_GET(final K key) {
        try {
            byte[] newKey = buildKey(key);
            byte[] bytes = jedisGet(newKey);
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
        }
    }

    @Override
    protected MultiGetResult<K, V> do_GET_ALL(final Set<? extends K> keys) {
        try {
            ArrayList<K> keyList = new ArrayList<K>(keys);
            byte[][] newKeys = keyList.stream().map(k -> buildKey(k)).toArray(byte[][]::new);

            Map<K, CacheGetResult<V>> resultMap = new HashMap<>();
            if (newKeys.length > 0) {
                List mgetResults = jedisMget(newKeys);
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
        } catch (Exception ex) {
            logError("GET_ALL", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<K, V>(ex);
        }
    }


    @Override
    protected CacheResult do_PUT(final K key, final V value, final long expireAfterWrite, final TimeUnit timeUnit) {
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            String rt = jedisPsetex(newKey, timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
            if ("OK".equals(rt)) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, rt);
            }
        } catch (Exception ex) {
            logError("PUT", key, ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_ALL(final Map<? extends K, ? extends V> map, final long expireAfterWrite,
            final TimeUnit timeUnit) {
        try {
            int failCount = 0;
            if (isEnablePipeline()) {
                failCount = doPutAllWithPipeline(map, expireAfterWrite, timeUnit);
            } else {
                failCount = doPutAllWithoutPipeline(map, expireAfterWrite, timeUnit);
            }
            return failCount == 0 ?
                    CacheResult.SUCCESS_WITHOUT_MSG :
                    failCount == map.size() ? CacheResult.FAIL_WITHOUT_MSG : CacheResult.PART_SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            logError("PUT_ALL", "map(" + map.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    private int doPutAllWithPipeline(final Map<? extends K, ? extends V> map, final long expireAfterWrite,
            final TimeUnit timeUnit) throws IOException {
        try (AbstractJedisPipeline p = getJedisPipeline()) {
            int failCount = 0;
            List<Response<String>> responses = new ArrayList<>();
            for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
                CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), timeUnit.toMillis(expireAfterWrite));
                Response<String> resp = p
                        .psetex(buildKey(en.getKey()), timeUnit.toMillis(expireAfterWrite), valueEncoder.apply(holder));
                responses.add(resp);
            }
            p.sync();
            for (Response<String> resp : responses) {
                if (!"OK".equals(resp.get())) {
                    failCount++;
                }
            }
            return failCount;
        }
    }

    private int doPutAllWithoutPipeline(final Map<? extends K, ? extends V> map, final long expireAfterWrite,
            final TimeUnit timeUnit) {
        int failCount = 0;
        for (Map.Entry<? extends K, ? extends V> en : map.entrySet()) {
            CacheValueHolder<V> holder = new CacheValueHolder(en.getValue(), timeUnit.toMillis(expireAfterWrite));
            String rt = jedisPsetex(buildKey(en.getKey()), timeUnit.toMillis(expireAfterWrite),
                    valueEncoder.apply(holder));
            if (!"OK".equals(rt)) {
                failCount++;
            }
        }
        return failCount;
    }

    @Override
    protected CacheResult do_REMOVE(final K key) {
        return REMOVE_impl(key, buildKey(key));
    }

    private CacheResult REMOVE_impl(final Object key, final byte[] newKey) {
        try {
            Long rt = jedisDel(newKey);
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
        }
    }

    @Override
    protected CacheResult do_REMOVE_ALL(final Set<? extends K> keys) {
        try {
            byte[][] newKeys = keys.stream().map(k -> buildKey(k)).toArray((len) -> new byte[keys.size()][]);
            jedisDel(newKeys);
            return CacheResult.SUCCESS_WITHOUT_MSG;
        } catch (Exception ex) {
            logError("REMOVE_ALL", "keys(" + keys.size() + ")", ex);
            return new CacheResult(ex);
        }
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(final K key, V value, final long expireAfterWrite, final TimeUnit timeUnit) {
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, timeUnit.toMillis(expireAfterWrite));
            byte[] newKey = buildKey(key);
            SetParams params = new SetParams();
            params.nx().px(timeUnit.toMillis(expireAfterWrite));
            String rt = jedisSet(newKey, valueEncoder.apply(holder), params);
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
    protected boolean needLogStackTrace(final Throwable e) {
        if (e instanceof JedisConnectionException) {
            return false;
        }
        return true;
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }
}
