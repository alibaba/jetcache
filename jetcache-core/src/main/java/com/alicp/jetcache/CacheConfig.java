package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;

import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfig implements Cloneable {
//    private long defaultExpireInMillis = CacheConsts.DEFAULT_EXPIRE * 1000L;
//    private boolean expireAfterAccess = false;
    private long expireAfterWriteInMillis = CacheConsts.DEFAULT_EXPIRE * 1000L;
    private long expireAfterAccessInMillis = -1;
    private Function<Object,Object> keyConvertor;

    @Override
    public CacheConfig clone() {
        try {
            return (CacheConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    public Function<Object,Object> getKeyConvertor() {
        return keyConvertor;
    }

    public void setKeyConvertor(Function<Object,Object> keyConvertor) {
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
}
