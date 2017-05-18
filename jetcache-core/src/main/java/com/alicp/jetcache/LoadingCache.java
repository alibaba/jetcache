package com.alicp.jetcache;

/**
 * Created on 2017/5/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LoadingCache<K, V> extends SimpleProxyCache<K,V> {

    public LoadingCache(Cache<K, V> cache) {
        super(cache);
    }

}
