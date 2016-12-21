package com.alicp.jetcache;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheValueHolder<V> {
    private V value;
    private long createTime;
    private long initTtlInMillis;
    private long expireTime;

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
