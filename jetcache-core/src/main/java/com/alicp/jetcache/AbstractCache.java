package com.alicp.jetcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractCache<K, V> implements Cache<K, V>, WrapValueCache<K, V> {

    private static Logger WRAP_VALUE_CACHE_INTERNAL_LOGGER = LoggerFactory.getLogger(AbstractCache.class);

    @Override
    public CacheGetResult<V> GET(K key) {
        CacheGetResult<V> result;
        try {
            CacheGetResult<CacheValueHolder<V>> holderResult = GET_HOLDER(key);
            result = (CacheGetResult<V>) holderResult;
            if (holderResult.getValue() != null) {
                result.setValue(holderResult.getValue().getValue());
            }
        } catch (ClassCastException ex) {
            WRAP_VALUE_CACHE_INTERNAL_LOGGER.warn("jetcache GET error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            result = new CacheGetResult<>(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage(), null);
        }
        return result;
    }
}
