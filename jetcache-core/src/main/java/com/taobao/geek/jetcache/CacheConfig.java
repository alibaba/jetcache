/**
 * Created on  13-09-10 10:33
 */
package com.taobao.geek.jetcache;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheConfig {
    private String area = CacheConsts.DEFAULT_AREA;
    private boolean enabled = CacheConsts.DEFAULT_ENABLED;
    private int expire = CacheConsts.DEFAULT_EXPIRE;
    private CacheType cacheType = CacheConsts.DEFAULT_CACHE_TYPE;
    private int localLimit = CacheConsts.DEFAULT_LOCAL_LIMIT;
    private int version = CacheConsts.DEFAULT_VERSION;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CacheConfig)) {
            return false;
        }
        CacheConfig cc = (CacheConfig) obj;
        return equals(area, cc.area) &&
                enabled == cc.enabled && expire == cc.expire
                && equals(cacheType, cc.cacheType) && localLimit == cc.localLimit
                && version == cc.version;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (area != null) {
            hash += area.hashCode();
        }
        hash = hash << 8 + expire;
        if (cacheType != null) {
            hash = hash << 2 + cacheType.hashCode();
        }
        hash = hash << 8 + localLimit;
        hash =  hash << 4 + version;

        if (enabled) {
            hash = ~hash;
        }
        return hash;
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 != null) {
            return o1.equals(o2);
        } else {
            return o2 == null;
        }
    }

    public String getArea() {
        return area;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getExpire() {
        return expire;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public int getLocalLimit() {
        return localLimit;
    }

    public int getVersion() {
        return version;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public void setLocalLimit(int localLimit) {
        this.localLimit = localLimit;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
