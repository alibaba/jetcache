package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfig<K, V> implements Cloneable {
    //    private long defaultExpireInMillis = CacheConsts.DEFAULT_EXPIRE * 1000L;
//    private boolean expireAfterAccess = false;
    private long expireAfterWriteInMillis = CacheConsts.DEFAULT_EXPIRE * 1000L;
    private long expireAfterAccessInMillis = -1;
    private Function<K, Object> keyConvertor;

    private Function<K, V> loader;
    private Function<Set<K>, Map<K, V>> batchLoader;

    private List<CacheMonitor> monitors = new ArrayList<>();

    private boolean cacheNullValueByDefault = false;

    @Override
    public CacheConfig clone() {
        try {
            CacheConfig copy = (CacheConfig) super.clone();
            copy.monitors = new ArrayList(monitors);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    public Function<K, Object> getKeyConvertor() {
        return keyConvertor;
    }

    public void setKeyConvertor(Function<K, Object> keyConvertor) {
        this.keyConvertor = keyConvertor;
    }

    public boolean isExpireAfterAccess() {
        return expireAfterAccessInMillis > 0;
    }

    public boolean isExpireAfterWrite() {
        return expireAfterWriteInMillis > 0;
    }

    @Deprecated
    public long getDefaultExpireInMillis() {
        return expireAfterWriteInMillis;
    }

    @Deprecated
    public void setDefaultExpireInMillis(long defaultExpireInMillis) {
        this.expireAfterWriteInMillis = defaultExpireInMillis;
    }

    public long getExpireAfterWriteInMillis() {
        return expireAfterWriteInMillis;
    }

    public void setExpireAfterWriteInMillis(long expireAfterWriteInMillis) {
        this.expireAfterWriteInMillis = expireAfterWriteInMillis;
    }

    public long getExpireAfterAccessInMillis() {
        return expireAfterAccessInMillis;
    }

    public void setExpireAfterAccessInMillis(long expireAfterAccessInMillis) {
        this.expireAfterAccessInMillis = expireAfterAccessInMillis;
    }

    public Function<K, V> getLoader() {
        return loader;
    }

    public void setLoader(Function<K, V> loader) {
        this.loader = loader;
    }

    public Function<Set<K>, Map<K, V>> getBatchLoader() {
        return batchLoader;
    }

    public void setBatchLoader(Function<Set<K>, Map<K, V>> batchLoader) {
        this.batchLoader = batchLoader;
    }

    public boolean isCacheNullValueByDefault() {
        return cacheNullValueByDefault;
    }

    public void setCacheNullValueByDefault(boolean cacheNullValueByDefault) {
        this.cacheNullValueByDefault = cacheNullValueByDefault;
    }

    public List<CacheMonitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<CacheMonitor> monitors) {
        this.monitors = monitors;
    }
}
