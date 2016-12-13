package com.alicp.jetcache;

/**
 * Created on 2016/11/1.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
interface WrapValueCache<K,V> extends Cache<K,V> {
    CacheGetResult<CacheValueHolder<V>> __GET_HOLDER(K key);

    @Override
    CacheGetResult<V> GET(K key);
}
