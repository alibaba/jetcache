package com.alicp.jetcache;

import java.io.Serializable;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public final class CacheValueHolder<V> implements Serializable {
    private static final long serialVersionUID = -7973743507831565203L;
    private V value;
    private long createTime;
    @Deprecated
    private long initTtlInMillis;
    private long expireTime;
    private long accessTime;

    /**
     * used by kyro
     */
    public CacheValueHolder() {
    }

    public CacheValueHolder(V value, long expireAfterWrite) {
        this.value = value;
        this.createTime = System.currentTimeMillis();
        this.accessTime = createTime;
        this.expireTime = createTime + expireAfterWrite;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Deprecated
    public long getInitTtlInMillis() {
        return initTtlInMillis;
    }

    @Deprecated
    public void setInitTtlInMillis(long initTtlInMillis) {
        this.initTtlInMillis = initTtlInMillis;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }
}
