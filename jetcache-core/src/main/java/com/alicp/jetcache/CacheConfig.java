package com.alicp.jetcache;

import com.alicp.jetcache.anno.CacheConsts;

import java.util.function.Function;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfig implements Cloneable {
    private long defaultExpireInMillis = CacheConsts.DEFAULT_EXPIRE * 1000;
    private boolean expireAfterAccess = false;
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
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(boolean expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public long getDefaultExpireInMillis() {
        return defaultExpireInMillis;
    }

    public void setDefaultExpireInMillis(long defaultExpireInMillis) {
        this.defaultExpireInMillis = defaultExpireInMillis;
    }
}
