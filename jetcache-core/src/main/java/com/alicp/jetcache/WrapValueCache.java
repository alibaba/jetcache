package com.alicp.jetcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface WrapValueCache<K, V> extends Cache<K, V> {

    Logger WRAP_VALUE_CACHE_INTERNAL_LOGGER = LoggerFactory.getLogger(WrapValueCache.class);

    CacheGetResult<CacheValueHolder<V>> GET_HOLDER(K key);

    @Override
    default CacheGetResult<V> GET(K key) {
        try {
            CacheGetResult<CacheValueHolder<V>> result = GET_HOLDER(key);
            CacheGetResult<V> newResult = (CacheGetResult<V>) result;
            if (result.getValue() != null) {
                newResult.setValue(result.getValue().getValue());
            }
            return newResult;
        } catch (ClassCastException ex) {
            WRAP_VALUE_CACHE_INTERNAL_LOGGER.warn("jetcache GET error. key={}, Exception={}, Message:{}", key, ex.getClass(), ex.getMessage());
            return new CacheGetResult<V>(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage(), null);
        }
    }
}
