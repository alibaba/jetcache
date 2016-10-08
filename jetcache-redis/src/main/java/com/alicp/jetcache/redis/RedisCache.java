package com.alicp.jetcache.redis;

import com.alibaba.fastjson.JSON;
import com.alicp.jetcache.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RedisCache<K, V> implements WapperValueCache<K, V> {

    private RedisCacheConfig config;

    private final Function<Object, Object> keyConvertor;
    Function<Object, byte[]> valueEncoder;
    Function<byte[], Object> valueDecoder;
    private JedisPool jedisPool;

    public RedisCache(RedisCacheConfig config) {
        this.config = config;
        this.jedisPool = config.getJedisPool();
        this.keyConvertor = config.getKeyConvertor();
        this.valueEncoder = config.getValueEncoder();
        this.valueDecoder = config.getValueDecoder();
    }

    @Override
    public CacheConfig config() {
        return config;
    }

    private byte[] buildKey(K key) throws UnsupportedEncodingException {
        Object newKey = keyConvertor.apply(key);
        if (newKey instanceof String) {
            String s = config.getKeyPrefix() + newKey;
            return s.getBytes("UTF-8");
        } else if (newKey instanceof byte[]) {
            byte[] bs1 = config.getKeyPrefix().getBytes("UTF-8");
            byte[] bs2 = (byte[]) newKey;
            byte[] rt = new byte[bs1.length + bs2.length];
            System.arraycopy(bs1, 0, rt, 0, bs1.length);
            System.arraycopy(bs2, 0, rt, bs1.length, bs2.length);
            return rt;
        } else {
            throw new CacheException("type error");//TODO ?
        }
    }

    @Override
    public CacheGetResult<CacheValueHolder<V>> GET_HOLDER(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] newKey = buildKey(key);
            Pipeline p = jedis.pipelined();
            Response<byte[]> valueResp = p.get(newKey);
            Response<Long> pttlResp = p.pttl(newKey);
            p.sync();
            if (valueResp.get() != null) {
                CacheValueHolder<V> holder = (CacheValueHolder<V>) valueDecoder.apply(valueResp.get());
                Long restTtl = pttlResp.get();
                if (restTtl != null) {
                    holder.setExpireTime(System.currentTimeMillis() + restTtl);
                }
                if (config.isExpireAfterAccess()) {
                    jedis.pexpire(newKey, holder.getInitTtlInMillis());
                }
                return new CacheGetResult(CacheResultCode.SUCCESS, null, holder);
            } else {
                return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CacheGetResult(CacheResultCode.FAIL, e.getClass() + ":" + e.getMessage(), null);
        }
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        try (Jedis jedis = jedisPool.getResource()) {
            CacheValueHolder<V> holder = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));

            byte[] newKey = buildKey(key);
            Pipeline p = jedis.pipelined();
            Response<String> rt = p.set(newKey, valueEncoder.apply(holder));
            p.pexpire(newKey, timeUnit.toMillis(expire));
            p.sync();

            if ("OK".equals(rt.get())) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, rt.get());
            }
        } catch (Exception e) {
            return new CacheResult(CacheResultCode.FAIL, e.getClass() + ":" + e.getMessage());
        }
    }

    private int convertTtl(long expire, TimeUnit timeUnit) {
        long t = timeUnit.toSeconds(expire);
        if (t == 0 && expire > 0) {
            return 1;
        } else {
            return (int) t;
        }
    }


    @Override
    public CacheResult INVALIDATE(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            Long rt = jedis.del(buildKey(key));
            if (rt != null && rt == 1) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return CacheResult.FAIL_WITHOUT_MSG;
            }
        } catch (Exception e) {
            return new CacheResult(CacheResultCode.FAIL, e.getClass() + ":" + e.getMessage());
        }
    }
}
