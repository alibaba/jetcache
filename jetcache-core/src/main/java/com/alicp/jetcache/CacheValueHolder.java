package com.alicp.jetcache;

import java.io.Serializable;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheValueHolder<V> implements Serializable {
    private static final long serialVersionUID = -7973743507831565203L;
    private V value;
    private long createTime;
    private long initTtlInMillis;
    private long expireTime;

    /**
     * used by kyro
     */
    public CacheValueHolder(){
    }

    public CacheValueHolder(V value, long createTime, long initTtlInMillis){
        this.value = value;
        this.createTime = createTime;
        this.initTtlInMillis = initTtlInMillis;
        this.expireTime = createTime + initTtlInMillis;
    }

    public long getInitTtlInMillis() {
        return initTtlInMillis;
    }

    public long getCreateTime() {
        return createTime;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
