package com.alicp.jetcache.cache;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheValueHolder<V> {
    private V value;
    private long createTime;
    private long expireTime;

    public CacheValueHolder(V value, long createTime, long expireTime){
        this.value = value;
        this.createTime = createTime;
        this.expireTime = expireTime;
    }

    public CacheValueHolder(){
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

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
