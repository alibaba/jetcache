package com.alicp.jetcache;

/**
 * Created on 2016/11/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public interface CacheBuilder {
    <K, V> Cache<K, V> buildCache();
}
