package com.alicp.jetcache;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface WapperValueCache<K, V> extends Cache<K, V> {
    CacheGetResult<CacheValueHolder<V>> GET_HOLDER(K key);

    @Override
    default CacheGetResult<V> GET(K key) {
        CacheGetResult<CacheValueHolder<V>> result = GET_HOLDER(key);
        CacheGetResult<V> newResult = (CacheGetResult<V>) result;
        if (result.getValue() != null) {
            newResult.setValue(result.getValue().getValue());
        }
        return newResult;
    }
}
