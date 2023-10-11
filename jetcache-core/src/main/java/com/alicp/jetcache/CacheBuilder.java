package com.alicp.jetcache;

/**
 * Created on 2016/11/17.
 *
 * @author huangli
 */
public interface CacheBuilder {
    <K, V> Cache<K, V> buildCache();
}
