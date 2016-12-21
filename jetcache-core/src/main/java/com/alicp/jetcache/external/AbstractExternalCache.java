package com.alicp.jetcache.external;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;

import java.io.UnsupportedEncodingException;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractExternalCache<K, V> extends AbstractCache<K, V> {

    private ExternalCacheConfig config;

    public AbstractExternalCache(ExternalCacheConfig config) {
        this.config = config;
        if (config.getKeyConvertor() == null) {
            throw new CacheConfigException("no KeyConvertor");
        }
        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }
    }

    protected byte[] buildKey(K key) {
        try {
            Object newKey = config.getKeyConvertor().apply(key);
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
                throw new CacheException("type error");
            }
        } catch (UnsupportedEncodingException e) {
            throw new CacheException(e);
        }
    }
}
