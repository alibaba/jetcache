/**
 * Created on  13-09-14 11:12
 */
package com.alicp.jetcache.ali.tair;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TairCache<K, V> extends AbstractExternalCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(TairCache.class);
    private TairCacheConfig config;

    public TairCache(TairCacheConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public CacheConfig config() {
        return config;
    }

    private Serializable buildTairKey(K key) {
        if (config.getKeyConvertor() != null) {
            return (Serializable) config.getKeyConvertor().apply(new Object[]{config.getKeyPrefix(), key});
        } else {
            return new Object[]{config.getKeyPrefix(), key};
        }
    }

    @Override
    public CacheGetResult<CacheValueHolder<V>> __GET_HOLDER(K key) {
        try {
            Result<DataEntry> tairResult = config.getTairManager().get(config.getNamespace(), buildTairKey(key));
            if (tairResult.isSuccess()) {
                DataEntry dn = tairResult.getValue();
                if (dn != null && dn.getValue() != null) {
                    byte[] bytes = (byte[]) dn.getValue();
                    CacheValueHolder<V> tv = (CacheValueHolder<V>) config.getValueDecoder().apply(bytes);
                    if (System.currentTimeMillis() > tv.getExpireTime()) {
                        return CacheGetResult.EXPIRED_WITHOUT_MSG;
                    } else {
                        return new CacheGetResult(CacheResultCode.SUCCESS, null, tv);
                    }
                } else {
                    return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
                }
            } else {
                int tairRc = tairResult.getRc().getCode();
                if (tairRc == ResultCode.DATANOTEXSITS.getCode()) {
                    return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
                } else if (tairRc == ResultCode.DATAEXPIRED.getCode()) {
                    return CacheGetResult.EXPIRED_WITHOUT_MSG;
                } else {
                    return new CacheGetResult(CacheResultCode.FAIL, tairRc + ":" + tairResult.getRc().getMessage(), null);
                }
            }
        } catch (Exception ex) {
            logger.warn("jetcache(TairCache) GET error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return new CacheGetResult(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage(), null);
        }
    }

    @Override
    public CacheResult PUT(K key, V value, long expire, TimeUnit timeUnit) {
        try {
            CacheValueHolder<V> holder = new CacheValueHolder(value, System.currentTimeMillis(), timeUnit.toMillis(expire));
            byte[] bytes = config.getValueEncoder().apply(holder);
            ResultCode tairCode = config.getTairManager().put(config.getNamespace(), buildTairKey(key), bytes, 0, (int) timeUnit.toSeconds(expire));
            if (tairCode.getCode() == ResultCode.SUCCESS.getCode()) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, tairCode.getCode() + ":" + tairCode.getMessage());
            }
        } catch (Exception ex) {
            logger.warn("jetcache(TairCache) PUT error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return new CacheResult(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage());
        }
    }

    @Override
    public CacheResult INVALIDATE(K key) {
        try {
            ResultCode tairCode = config.getTairManager().invalid(config.getNamespace(), buildTairKey(key));
            if (tairCode.getCode() == ResultCode.SUCCESS.getCode()) {
                return CacheResult.SUCCESS_WITHOUT_MSG;
            } else {
                return new CacheResult(CacheResultCode.FAIL, tairCode.getCode() + ":" + tairCode.getMessage());
            }
        } catch (Exception ex) {
            logger.warn("jetcache(TairCache) INVALIDATE error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return new CacheResult(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage());
        }
    }

}
